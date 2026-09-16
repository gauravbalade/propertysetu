package com.gaurav.property.dto;

import java.time.LocalDateTime;

import com.gaurav.property.enums.VerificationStatus;

public record VerificationResponse(
        Long id,
        ApplicationResponse application,
        LocalDateTime verificationDate,
        VerificationStatus status,
        String remarks,
        String verifiedByUsername) {
}
