package com.gaurav.property.dto;

import java.time.LocalDateTime;

public record AuditEventResponse(
        Long id,
        String action,
        String entityType,
        String entityId,
        String actorUsername,
        String actorRole,
        String details,
        LocalDateTime occurredAt) {
}
