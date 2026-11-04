package com.gaurav.property.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.ApplicationResponse;
import com.gaurav.property.dto.VerificationRequest;
import com.gaurav.property.dto.VerificationResponse;
import com.gaurav.property.entity.Document;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.entity.Verification;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.enums.DocumentStatus;
import com.gaurav.property.enums.PaymentStatus;
import com.gaurav.property.enums.VerificationStatus;
import com.gaurav.property.repository.DocumentRepository;
import com.gaurav.property.repository.PaymentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;
import com.gaurav.property.repository.VerificationRepository;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final RegistrationApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final PaymentRepository paymentRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public VerificationService(
            VerificationRepository verificationRepository,
            RegistrationApplicationRepository applicationRepository,
            DocumentRepository documentRepository,
            PaymentRepository paymentRepository,
            AuthorizationService authorizationService,
            AuditService auditService,
            NotificationService notificationService) {
        this.verificationRepository = verificationRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.paymentRepository = paymentRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public VerificationResponse verifyApplication(VerificationRequest request) {
        RegistrationApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        authorizationService.requireAssignedOfficer(application);
        UserAccount officer = authorizationService.currentUser();

        if (request.getVerifiedByUserId() != null
                && !officer.getId().equals(request.getVerifiedByUserId())) {
            throw new RuntimeException("The authenticated officer must verify the application");
        }

        ApplicationStatus currentStatus = application.getStatus();
        if (currentStatus != ApplicationStatus.PAID
                && currentStatus != ApplicationStatus.UNDER_VERIFICATION
                && currentStatus != ApplicationStatus.RESUBMITTED) {
            throw new RuntimeException("Only paid or resubmitted applications can be verified.");
        }

        boolean paymentConfirmed = paymentRepository.findByApplicationId(application.getId()).stream()
                .anyMatch(payment -> payment.getPaymentStatus() == PaymentStatus.SUCCESS
                        && Boolean.TRUE.equals(payment.getSignatureVerified())
                        && "RAZORPAY".equalsIgnoreCase(payment.getGatewayReference()));
        if (!paymentConfirmed) {
            throw new RuntimeException("A server-confirmed payment is required before verification.");
        }

        application.setStatus(ApplicationStatus.UNDER_VERIFICATION);

        Map<String, Document> latestByType = new HashMap<>();
        List<Document> documents = documentRepository.findByApplicationId(application.getId());
        for (Document document : documents) {
            if (document.getStatus() == DocumentStatus.SUPERSEDED) continue;
            Document previous = latestByType.get(document.getDocumentType());
            if (previous == null || (document.getVersionNumber() != null
                    && (previous.getVersionNumber() == null
                    || document.getVersionNumber() > previous.getVersionNumber()))) {
                latestByType.put(document.getDocumentType(), document);
            }
        }

        Verification verification = verificationRepository.findByApplicationId(application.getId())
                .orElse(new Verification());
        verification.setApplication(application);
        verification.setVerifiedBy(officer);
        verification.setVerificationDate(LocalDateTime.now());
        verification.setStatus(request.getStatus());
        verification.setRemarks(request.getRemarks().trim());

        if (request.getStatus() == VerificationStatus.VERIFIED) {
            List<String> requiredTypes = List.of("IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT");
            List<String> missingOrUnverified = requiredTypes.stream()
                    .filter(type -> {
                        Document latest = latestByType.get(type);
                        return latest == null || latest.getStatus() != DocumentStatus.VERIFIED;
                    })
                    .toList();

            if (!missingOrUnverified.isEmpty()) {
                throw new RuntimeException("Verify the latest version of every required document before completing: "
                        + String.join(", ", missingOrUnverified));
            }
            application.setStatus(ApplicationStatus.COMPLETED);
        } else if (request.getStatus() == VerificationStatus.REJECTED) {
            if (request.getRemarks() == null || request.getRemarks().isBlank()) {
                throw new RuntimeException("A reason is required when rejecting an application.");
            }
            application.setStatus(ApplicationStatus.REJECTED);
        }

        applicationRepository.save(application);
        Verification saved = verificationRepository.save(verification);
        auditService.record("APPLICATION_VERIFIED", "APPLICATION", application.getId(),
                officer, "Verification status: " + request.getStatus().name()
                        + ". Remarks: " + request.getRemarks().trim());

        String nextStep = request.getStatus() == VerificationStatus.VERIFIED
                ? "The academic verification workflow is complete. This is not an official government registration certificate."
                : request.getStatus() == VerificationStatus.REJECTED
                        ? "The application was rejected in the academic workflow. Review the officer's reason in your account."
                        : "Your application remains in the assigned officer's review queue.";

        notificationService.sendApplicationStatus(
                application.getUserAccount().getEmail(),
                application.getApplicationNumber(),
                application.getProperty().getPropertyNumber(),
                application.getStatus().name(),
                nextStep);

        ApplicationResponse applicationResponse = new ApplicationResponse(
                application.getId(),
                application.getApplicationNumber(),
                application.getUserAccount().getId(),
                application.getUserAccount().getUsername(),
                application.getUserAccount().getEmail(),
                application.getProperty().getId(),
                application.getProperty().getPropertyNumber(),
                application.getPurpose(),
                application.getApplicationDate(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getAssignedOfficer() == null ? null : application.getAssignedOfficer().getId(),
                application.getAssignedOfficer() == null ? null : application.getAssignedOfficer().getUsername(),
                application.getAssignedAt());

        return new VerificationResponse(
                saved.getId(),
                applicationResponse,
                saved.getVerificationDate(),
                saved.getStatus(),
                saved.getRemarks(),
                saved.getVerifiedBy() == null ? null : saved.getVerifiedBy().getUsername());
    }
}
