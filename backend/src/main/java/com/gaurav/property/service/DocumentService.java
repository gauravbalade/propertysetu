package com.gaurav.property.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.gaurav.property.dto.DocumentResponse;
import com.gaurav.property.entity.Document;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.enums.DocumentStatus;
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.repository.DocumentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;

@Service
public class DocumentService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "IDENTITY_PROOF", "ADDRESS_PROOF", "PROPERTY_DOCUMENT",
            "TITLE_DOCUMENT", "TAX_RECEIPT", "STAMP_DUTY_PROOF",
            "NOC_OR_APPROVAL", "OTHER");

    private final DocumentRepository documentRepository;
    private final RegistrationApplicationRepository applicationRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    @Value("${app.upload.dir}")
    private String uploadDirectory;

    public DocumentService(DocumentRepository documentRepository,
            RegistrationApplicationRepository applicationRepository,
            AuthorizationService authorizationService,
            AuditService auditService) {
        this.documentRepository = documentRepository;
        this.applicationRepository = applicationRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    public DocumentResponse uploadDocument(Long applicationId, String documentType, MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select a file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size must not exceed 5 MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new RuntimeException("Invalid file name");
        }

        String normalizedType = documentType == null ? ""
                : documentType.trim().toUpperCase().replace(' ', '_');
        if (!ALLOWED_TYPES.contains(normalizedType)) {
            throw new RuntimeException("Choose a valid document category");
        }

        String lowerName = originalFileName.toLowerCase();
        if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".jpg")
                && !lowerName.endsWith(".jpeg") && !lowerName.endsWith(".png")) {
            throw new RuntimeException("Only PDF, JPG, JPEG and PNG files are allowed");
        }
        validateFileSignature(file, lowerName);

        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        assertCanAccess(application);
        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException("Documents can only be uploaded while the application is in DRAFT status");
        }

        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        String safeFileName = System.currentTimeMillis() + "_"
                + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path targetPath = uploadPath.resolve(safeFileName).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            throw new RuntimeException("Invalid storage path");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setApplication(application);
        document.setDocumentType(normalizedType);
        document.setFileName(originalFileName);
        document.setStoredPath(targetPath.toString());
        document.setUploadDate(LocalDateTime.now());
        document.setStatus(DocumentStatus.UPLOADED);
        Document saved = documentRepository.save(document);
        auditService.record("DOCUMENT_UPLOADED", "APPLICATION", applicationId,
                authorizationService.currentUser(), "Uploaded " + normalizedType);
        return toResponse(saved);
    }

    public List<DocumentResponse> getDocuments(Long applicationId) {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        assertCanAccess(application);
        return documentRepository.findByApplicationId(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DocumentResponse reviewDocument(Long applicationId, Long documentId, DocumentStatus status) {
        if (status != DocumentStatus.VERIFIED && status != DocumentStatus.REJECTED
                && status != DocumentStatus.UNDER_REVIEW) {
            throw new RuntimeException("Choose VERIFIED, REJECTED or UNDER_REVIEW");
        }
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        requireOfficer();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getApplication().getId().equals(application.getId())) {
            throw new RuntimeException("Document does not belong to this application");
        }
        document.setStatus(status);
        Document saved = documentRepository.save(document);
        auditService.record("DOCUMENT_REVIEWED", "DOCUMENT", documentId,
                authorizationService.currentUser(), "Status changed to " + status.name());
        auditService.record("DOCUMENT_STATUS_CHANGED", "APPLICATION", applicationId,
                authorizationService.currentUser(),
                normalizedDocumentType(document.getDocumentType()) + " document marked " + status.name());
        return toResponse(saved);
    }

    public void deleteDocument(Long applicationId, Long documentId) throws IOException {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        assertCanAccess(application);

        if (application.getStatus() != ApplicationStatus.DRAFT) {
            throw new RuntimeException("Documents can only be removed while the application is in DRAFT status");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getApplication().getId().equals(applicationId)) {
            throw new RuntimeException("Document does not belong to this application");
        }

        Path storedPath = Paths.get(document.getStoredPath()).toAbsolutePath().normalize();
        Path uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        if (storedPath.startsWith(uploadRoot) && Files.isRegularFile(storedPath)) {
            Files.deleteIfExists(storedPath);
        }

        documentRepository.delete(document);
        auditService.record("DOCUMENT_REMOVED", "APPLICATION", applicationId,
                authorizationService.currentUser(), "Removed " + document.getDocumentType());
    }

    public DocumentDownload downloadDocument(Long applicationId, Long documentId) throws IOException {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        assertCanAccess(application);
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        if (!document.getApplication().getId().equals(application.getId())) {
            throw new RuntimeException("Document does not belong to this application");
        }

        Path storedPath = Paths.get(document.getStoredPath()).toAbsolutePath().normalize();
        Path uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        if (!storedPath.startsWith(uploadRoot) || !Files.isRegularFile(storedPath)) {
            throw new RuntimeException("Stored document is unavailable");
        }
        Resource resource = new UrlResource(storedPath.toUri());
        String contentType = Files.probeContentType(storedPath);
        return new DocumentDownload(resource, document.getFileName(),
                contentType == null ? "application/octet-stream" : contentType);
    }

    private String normalizedDocumentType(String type) {
        return String.valueOf(type).replace('_', ' ');
    }

    private void validateFileSignature(MultipartFile file, String lowerName) throws IOException {
        byte[] header = file.getBytes();
        boolean pdf = lowerName.endsWith(".pdf") && header.length >= 4
                && header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
        boolean jpeg = (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg"))
                && header.length >= 3 && (header[0] & 0xff) == 0xff
                && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff;
        boolean png = lowerName.endsWith(".png") && header.length >= 8
                && (header[0] & 0xff) == 0x89 && header[1] == 0x50
                && header[2] == 0x4e && header[3] == 0x47
                && header[4] == 0x0d && header[5] == 0x0a
                && header[6] == 0x1a && header[7] == 0x0a;
        if (!pdf && !jpeg && !png) {
            throw new RuntimeException("The file content does not match its declared document format");
        }
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(document.getId(), document.getApplication().getId(),
                document.getDocumentType(), document.getFileName(), document.getUploadDate(), document.getStatus());
    }

    private void assertCanAccess(RegistrationApplication application) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authentication is required");
        }
        if (isOfficer(authentication)) {
            return;
        }
        if (application.getUserAccount() == null
                || !authentication.getName().equals(application.getUserAccount().getUsername())) {
            throw new RuntimeException("You are not allowed to access this application");
        }
    }

    private void requireOfficer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !isOfficer(authentication)) {
            throw new RuntimeException("Officer access is required");
        }
    }

    private boolean isOfficer(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + UserRole.OFFICER.name())
                        || authority.getAuthority().equals("ROLE_" + UserRole.ADMIN.name()));
    }

    public record DocumentDownload(Resource resource, String fileName, String contentType) {
    }
}
