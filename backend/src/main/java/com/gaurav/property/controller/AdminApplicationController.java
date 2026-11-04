package com.gaurav.property.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.ApplicationAssignmentRequest;
import com.gaurav.property.dto.ApplicationResponse;
import com.gaurav.property.service.ApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/applications")
public class AdminApplicationController {

    private final ApplicationService applicationService;

    public AdminApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/{applicationId}/assignment")
    public ResponseEntity<ApplicationResponse> assignOfficer(
            @PathVariable Long applicationId,
            @Valid @RequestBody ApplicationAssignmentRequest request) {
        return ResponseEntity.ok(applicationService.assignOfficer(applicationId, request));
    }
}
