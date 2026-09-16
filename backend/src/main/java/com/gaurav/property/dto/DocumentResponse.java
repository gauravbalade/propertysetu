package com.gaurav.property.dto;

import java.time.LocalDateTime;

import com.gaurav.property.enums.DocumentStatus;

import lombok.Getter;

@Getter
public class DocumentResponse {

    private final Long id;
    private final Long applicationId;
    private final String documentType;
    private final String fileName;
    private final LocalDateTime uploadDate;
    private final DocumentStatus status;

    public DocumentResponse(Long id, Long applicationId, String documentType,
            String fileName, LocalDateTime uploadDate, DocumentStatus status) {
        this.id = id;
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.fileName = fileName;
        this.uploadDate = uploadDate;
        this.status = status;
    }
}
