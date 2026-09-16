package com.gaurav.property.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.AuditEventResponse;
import com.gaurav.property.service.AuditService;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<List<AuditEventResponse>> applicationHistory(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(auditService.applicationHistory(applicationId));
    }
}
