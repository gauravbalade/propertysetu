package com.gaurav.property.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gaurav.property.dto.AuditEventResponse;
import com.gaurav.property.entity.AuditEvent;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.repository.AuditEventRepository;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final AuthorizationService authorizationService;

    public AuditService(AuditEventRepository auditEventRepository,
            AuthorizationService authorizationService) {
        this.auditEventRepository = auditEventRepository;
        this.authorizationService = authorizationService;
    }

    public void record(String action, String entityType, Object entityId,
            UserAccount actor, String details) {
        AuditEvent event = new AuditEvent();
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(String.valueOf(entityId));
        event.setActorUsername(actor == null ? "SYSTEM" : actor.getUsername());
        event.setActorRole(actor == null || actor.getRole() == null ? "SYSTEM" : actor.getRole().name());
        event.setDetails(details);
        event.setOccurredAt(LocalDateTime.now());
        auditEventRepository.save(event);
    }

    public List<AuditEventResponse> applicationHistory(Long applicationId) {
        authorizationService.requireOfficer();
        return auditEventRepository.findByEntityTypeAndEntityIdOrderByOccurredAtDesc(
                        "APPLICATION", String.valueOf(applicationId))
                .stream().map(this::toResponse).toList();
    }

    private AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getAction(), event.getEntityType(),
                event.getEntityId(), event.getActorUsername(), event.getActorRole(),
                event.getDetails(), event.getOccurredAt());
    }
}
