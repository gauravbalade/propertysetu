package com.gaurav.property.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.OwnerRequest;
import com.gaurav.property.entity.Owner;
import com.gaurav.property.service.OwnerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping("/me")
    public ResponseEntity<Owner> getCurrentOwner() {
        return ownerService.getCurrentOwner()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public ResponseEntity<Owner> createOwner(
            @Valid @RequestBody OwnerRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ownerService.createOwner(request));
    }
}