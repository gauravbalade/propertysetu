package com.gaurav.property.dto;

import java.math.BigDecimal;

/**
 * Deliberately limited public view. Never expose Owner, UserAccount,
 * Document, stored paths, identity numbers, phone numbers or emails here.
 */
public record PublicPropertyResponse(
        Long id,
        String propertyNumber,
        String propertyType,
        BigDecimal area,
        String description,
        String registrationStatus,
        String ownerDisplayName,
        String ownerReference,
        String city,
        String district,
        String state,
        String verificationStatus,
        String applicationNumber) {
}
