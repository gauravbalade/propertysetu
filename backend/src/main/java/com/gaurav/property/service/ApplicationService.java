package com.gaurav.property.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gaurav.property.dto.ApplicationAssignmentRequest;
import com.gaurav.property.dto.ApplicationRequest;
import com.gaurav.property.dto.ApplicationResponse;
import com.gaurav.property.entity.Property;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.repository.PropertyRepository;
import com.gaurav.property.repository.DocumentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;
import com.gaurav.property.repository.UserAccountRepository;
import com.gaurav.property.enums.UserRole;

@Service
public class ApplicationService {

    private final RegistrationApplicationRepository applicationRepository;
    private final UserAccountRepository userAccountRepository;
    private final PropertyRepository propertyRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public ApplicationService(
            RegistrationApplicationRepository applicationRepository,
            UserAccountRepository userAccountRepository,
            PropertyRepository propertyRepository,
            DocumentRepository documentRepository,
            DocumentService documentService,
            AuthorizationService authorizationService,
            AuditService auditService,
            NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.userAccountRepository = userAccountRepository;
        this.propertyRepository = propertyRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    public RegistrationApplication createApplication(ApplicationRequest request) {
        UserAccount user = authorizationService.currentUser();
        if (!user.getId().equals(request.getUserId())) {
            throw new RuntimeException("You are not allowed to create an application for another user");
        }
        authorizationService.requireOwner(user);

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));
        authorizationService.requireOwner(property.getOwner().getUserAccount());

        List<ApplicationStatus> activeStatuses = List.of(
                ApplicationStatus.DRAFT,
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.PAYMENT_PENDING,
                ApplicationStatus.PAID,
                ApplicationStatus.UNDER_VERIFICATION,
                ApplicationStatus.NEEDS_CORRECTION,
                ApplicationStatus.RESUBMITTED,
                ApplicationStatus.VERIFIED);
        RegistrationApplication existing = applicationRepository
                .findFirstByPropertyIdAndUserAccountIdAndStatusIn(
                        property.getId(), user.getId(), activeStatuses)
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        RegistrationApplication application = new RegistrationApplication();
        application.setApplicationNumber(
                "REG-" + UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase());
        application.setUserAccount(user);
        application.setProperty(property);
        application.setApplicationDate(LocalDate.now());
        application.setPurpose(request.getPurpose());
        application.setStatus(ApplicationStatus.DRAFT);
        application.setCreatedAt(LocalDateTime.now());

        RegistrationApplication saved = applicationRepository.save(application);
        auditService.record("APPLICATION_CREATED", "APPLICATION", saved.getId(),
                user, "Draft application created for property " + property.getPropertyNumber());
        return saved;
    }

    public List<ApplicationResponse> getAllApplications() {
        var actor = authorizationService.currentUser();
        List<RegistrationApplication> applications;
        if (actor.getRole() == UserRole.ADMIN) {
            applications = applicationRepository.findAll();
        } else if (actor.getRole() == UserRole.OFFICER) {
            applications = applicationRepository.findByAssignedOfficerId(actor.getId());
        } else {
            applications = applicationRepository.findByUserAccountId(actor.getId());
        }
        return applications.stream().map(this::toResponse).toList();
    }

    public ApplicationResponse toResponse(RegistrationApplication application) {
        return new ApplicationResponse(
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
    }

    @org.springframework.transaction.annotation.Transactional
    public ApplicationResponse assignOfficer(Long applicationId, ApplicationAssignmentRequest request) {
        var admin = authorizationService.requireAdmin();
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        if (application.getStatus() != ApplicationStatus.PAID
                && application.getStatus() != ApplicationStatus.UNDER_VERIFICATION
                && application.getStatus() != ApplicationStatus.NEEDS_CORRECTION
                && application.getStatus() != ApplicationStatus.RESUBMITTED) {
            throw new RuntimeException("Only paid or review-stage applications can be assigned to an officer.");
        }
        UserAccount officer = userAccountRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new RuntimeException("Officer account not found"));
        if (officer.getRole() != UserRole.OFFICER || !Boolean.TRUE.equals(officer.getActive())) {
            throw new RuntimeException("Choose an active account with the OFFICER role.");
        }
        String previousOfficer = application.getAssignedOfficer() == null
                ? "unassigned" : application.getAssignedOfficer().getUsername();
        application.setAssignedOfficer(officer);
        application.setAssignedAt(LocalDateTime.now());
        application.setAssignedByUsername(admin.getUsername());
        RegistrationApplication saved = applicationRepository.save(application);
        auditService.record("APPLICATION_ASSIGNED", "APPLICATION", saved.getId(), admin,
                "Assignment changed from " + previousOfficer + " to " + officer.getUsername()
                        + ". Reason: " + request.getReason().trim());
        notificationService.sendApplicationStatus(
                saved.getUserAccount().getEmail(), saved.getApplicationNumber(),
                saved.getProperty().getPropertyNumber(), saved.getStatus().name(),
                "Your application has been assigned for officer review.");
        return toResponse(saved);
    }

    public RegistrationApplication updateApplication(Long applicationId, ApplicationRequest request) {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT applications can be edited.");
        }

        if (!application.getProperty().getId().equals(request.getPropertyId())) {
            throw new RuntimeException("The property connected to an application cannot be changed.");
        }

        application.setPurpose(request.getPurpose().trim());
        RegistrationApplication saved = applicationRepository.save(application);
        auditService.record("APPLICATION_UPDATED", "APPLICATION", applicationId,
                authorizationService.currentUser(), "Draft application details updated");
        return saved;
    }

    public void deleteApplication(Long applicationId) {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT applications can be deleted.");
        }

        try {
            documentService.removeAllDocumentsForApplication(applicationId);
        } catch (java.io.IOException ex) {
            throw new RuntimeException("Unable to remove the draft application documents safely.");
        }

        applicationRepository.delete(application);
        auditService.record("APPLICATION_DELETED", "APPLICATION", applicationId,
                authorizationService.currentUser(), "Draft application deleted");
    }

    @org.springframework.transaction.annotation.Transactional
    public RegistrationApplication submitApplication(Long applicationId) {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        boolean draft = application.getStatus() == ApplicationStatus.DRAFT;
        boolean correction = application.getStatus() == ApplicationStatus.NEEDS_CORRECTION;
        if (!draft && !correction) {
            throw new RuntimeException("Only a draft or a correction-requested application can be submitted.");
        }

        java.util.Map<String, com.gaurav.property.entity.Document> latestByType = new java.util.HashMap<>();
        for (var document : documentRepository.findByApplicationId(applicationId)) {
            if (document.getStatus() == com.gaurav.property.enums.DocumentStatus.SUPERSEDED) continue;
            var previous = latestByType.get(document.getDocumentType());
            if (previous == null || (document.getVersionNumber() != null
                    && (previous.getVersionNumber() == null
                    || document.getVersionNumber() > previous.getVersionNumber()))) {
                latestByType.put(document.getDocumentType(), document);
            }
        }

        List<String> requiredTypes = List.of("IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT");
        List<String> missingTypes = requiredTypes.stream()
                .filter(type -> {
                    var latest = latestByType.get(type);
                    return latest == null || latest.getStatus() == com.gaurav.property.enums.DocumentStatus.REJECTED;
                })
                .toList();

        if (!missingTypes.isEmpty()) {
            throw new RuntimeException("Upload or correct the required documents before submission: "
                    + String.join(", ", missingTypes));
        }

        ApplicationStatus nextStatus = correction
                ? ApplicationStatus.RESUBMITTED : ApplicationStatus.SUBMITTED;
        application.setStatus(nextStatus);
        RegistrationApplication saved = applicationRepository.save(application);
        auditService.record(correction ? "APPLICATION_RESUBMITTED" : "APPLICATION_SUBMITTED",
                "APPLICATION", applicationId, authorizationService.currentUser(),
                correction
                        ? "Applicant resubmitted corrected document versions for officer review"
                        : "Application submitted for payment and officer review");

        notificationService.sendApplicationStatus(
                application.getUserAccount().getEmail(),
                application.getApplicationNumber(),
                application.getProperty().getPropertyNumber(),
                application.getStatus().name(),
                correction
                        ? "Your corrected documents were resubmitted. The assigned officer can continue the review."
                        : "The next step is the payment stage. Continue from your PropertySetu workspace.");
        return saved;
    }
}