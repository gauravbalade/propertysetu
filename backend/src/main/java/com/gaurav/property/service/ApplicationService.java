package com.gaurav.property.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.gaurav.property.dto.ApplicationRequest;
import com.gaurav.property.dto.ApplicationResponse;
import com.gaurav.property.entity.Property;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.repository.PropertyRepository;
import com.gaurav.property.repository.DocumentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;

@Service
public class ApplicationService {

    private final RegistrationApplicationRepository applicationRepository;
    private final PropertyRepository propertyRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final NotificationService notificationService;

    public ApplicationService(
            RegistrationApplicationRepository applicationRepository,
            PropertyRepository propertyRepository,
            DocumentRepository documentRepository,
            DocumentService documentService,
            AuthorizationService authorizationService,
            AuditService auditService,
            NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.propertyRepository = propertyRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
        this.notificationService = notificationService;
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
        List<RegistrationApplication> applications = authorizationService.isOfficer()
                ? applicationRepository.findAll()
                : applicationRepository.findByUserAccountId(authorizationService.currentUser().getId());
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
                application.getCreatedAt());
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

    public RegistrationApplication submitApplication(Long applicationId) {
        RegistrationApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() ->
                                new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException(
                    "Only DRAFT applications can be submitted");
        }

        List<String> uploadedTypes = documentRepository.findByApplicationId(applicationId)
                .stream()
                .map(document -> document.getDocumentType())
                .toList();

        List<String> requiredTypes = List.of("IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT");
        List<String> missingTypes = requiredTypes.stream()
                .filter(type -> !uploadedTypes.contains(type))
                .toList();

        if (!missingTypes.isEmpty()) {
            throw new RuntimeException("Upload required documents before submission: "
                    + String.join(", ", missingTypes));
        }

        application.setStatus(ApplicationStatus.SUBMITTED);
        RegistrationApplication saved = applicationRepository.save(application);
        auditService.record("APPLICATION_SUBMITTED", "APPLICATION", applicationId,
                authorizationService.currentUser(), "Application submitted for review");

        notificationService.sendApplicationStatus(
                application.getUserAccount().getEmail(),
                application.getApplicationNumber(),
                application.getProperty().getPropertyNumber(),
                application.getStatus().name(),
                "The next step is the payment stage. Continue from your PropertySetu workspace.");
        return saved;
    }
}