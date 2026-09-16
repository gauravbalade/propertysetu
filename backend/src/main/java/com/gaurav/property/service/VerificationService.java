package com.gaurav.property.service;

import java.time.LocalDateTime;
import java.util.List;

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
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.enums.VerificationStatus;
import com.gaurav.property.repository.DocumentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;
import com.gaurav.property.repository.VerificationRepository;

@Service
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final RegistrationApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    public VerificationService(
            VerificationRepository verificationRepository,
            RegistrationApplicationRepository applicationRepository,
            DocumentRepository documentRepository,
            AuthorizationService authorizationService,
            AuditService auditService) {

        this.verificationRepository = verificationRepository;
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional
    public VerificationResponse verifyApplication(VerificationRequest request) {

        RegistrationApplication application =
                applicationRepository.findById(request.getApplicationId())
                        .orElseThrow(() ->
                                new RuntimeException("Application not found"));

        authorizationService.requireOfficer();
        UserAccount officer = authorizationService.currentUser();

        if (request.getVerifiedByUserId() != null
                && !officer.getId().equals(request.getVerifiedByUserId())) {
            throw new RuntimeException("The authenticated officer must verify the application");
        }

        if (officer.getRole() != UserRole.OFFICER
                && officer.getRole() != UserRole.ADMIN) {

            throw new RuntimeException(
                    "Only an officer or admin can verify applications");
        }

        ApplicationStatus currentStatus = application.getStatus();

        if (currentStatus != ApplicationStatus.PAID
                && currentStatus != ApplicationStatus.UNDER_VERIFICATION) {

            throw new RuntimeException(
                    "Only paid applications can be verified");
        }

        application.setStatus(ApplicationStatus.UNDER_VERIFICATION);

        Verification verification =
                verificationRepository
                        .findByApplicationId(application.getId())
                        .orElse(new Verification());

        verification.setApplication(application);
        verification.setVerifiedBy(officer);
        verification.setVerificationDate(LocalDateTime.now());
        verification.setStatus(request.getStatus());
        verification.setRemarks(request.getRemarks());

        if (request.getStatus() == VerificationStatus.VERIFIED) {
            List<Document> documents = documentRepository.findByApplicationId(application.getId());
            List<String> requiredTypes = List.of("IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT");
            List<String> missingOrUnverified = requiredTypes.stream()
                    .filter(type -> documents.stream().noneMatch(document ->
                            type.equals(document.getDocumentType())
                                    && document.getStatus() == DocumentStatus.VERIFIED))
                    .toList();

            if (!missingOrUnverified.isEmpty()) {
                throw new RuntimeException("Verify all required documents before completing the application: "
                        + String.join(", ", missingOrUnverified));
            }

            application.setStatus(ApplicationStatus.COMPLETED);

        } else if (request.getStatus() == VerificationStatus.REJECTED) {
            application.setStatus(ApplicationStatus.REJECTED);
        }

        applicationRepository.save(application);
        Verification saved = verificationRepository.save(verification);
        auditService.record("APPLICATION_VERIFIED", "APPLICATION", application.getId(),
                officer, "Verification status: " + request.getStatus().name());
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
                application.getCreatedAt());
        return new VerificationResponse(
                saved.getId(),
                applicationResponse,
                saved.getVerificationDate(),
                saved.getStatus(),
                saved.getRemarks(),
                saved.getVerifiedBy() == null ? null : saved.getVerifiedBy().getUsername());
    }
}