package com.gaurav.property.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.VerificationRequest;
import com.gaurav.property.dto.VerificationResponse;
import com.gaurav.property.service.VerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping
    public ResponseEntity<VerificationResponse> verifyApplication(
            @Valid @RequestBody VerificationRequest request) {

        return ResponseEntity.ok(
                verificationService.verifyApplication(request));
    }
}