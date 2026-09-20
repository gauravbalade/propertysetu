package com.gaurav.property.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gaurav.property.enums.DocumentStatus;

import com.gaurav.property.dto.DocumentResponse;
import com.gaurav.property.service.DocumentService;

@RestController
@RequestMapping("/api/applications")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/{applicationId}/documents")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long applicationId,
            @RequestParam String documentType,
            @RequestParam MultipartFile file) throws IOException {

        DocumentResponse document = documentService.uploadDocument(
                applicationId,
                documentType,
                file);

        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @GetMapping("/{applicationId}/documents")
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @PathVariable Long applicationId) {

        return ResponseEntity.ok(
                documentService.getDocuments(applicationId));
    }

    @DeleteMapping("/{applicationId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId) throws IOException {
        documentService.deleteDocument(applicationId, documentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{applicationId}/documents/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId) throws IOException {
        DocumentService.DocumentDownload download = documentService.downloadDocument(applicationId, documentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.fileName().replace("\"", "") + "\"")
                .header(HttpHeaders.CONTENT_TYPE, download.contentType())
                .body(download.resource());
    }

    @PostMapping("/{applicationId}/documents/{documentId}/review")
    public ResponseEntity<DocumentResponse> reviewDocument(
            @PathVariable Long applicationId,
            @PathVariable Long documentId,
            @RequestParam DocumentStatus status) {

        return ResponseEntity.ok(documentService.reviewDocument(
                applicationId, documentId, status));
    }
}