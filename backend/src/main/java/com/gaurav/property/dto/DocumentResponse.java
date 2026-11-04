package com.gaurav.property.dto;

import java.time.LocalDateTime;

import com.gaurav.property.enums.DocumentStatus;

public record DocumentResponse(
        Long id,
        Long applicationId,
        String documentType,
        String fileName,
        LocalDateTime uploadDate,
        DocumentStatus status,
        Integer versionNumber,
        String reviewRemarks,
        String reviewedByUsername,
        LocalDateTime reviewedAt) {
}
