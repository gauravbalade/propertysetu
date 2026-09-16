package com.gaurav.property.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gaurav.property.enums.ApplicationStatus;

public record ApplicationResponse(
        Long id,
        String applicationNumber,
        Long applicantId,
        String applicantUsername,
        String applicantEmail,
        Long propertyId,
        String propertyNumber,
        String purpose,
        LocalDate applicationDate,
        ApplicationStatus status,
        LocalDateTime createdAt) {
}
