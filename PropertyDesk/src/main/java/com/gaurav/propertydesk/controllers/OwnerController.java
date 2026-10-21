package com.gaurav.propertydesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gaurav.propertydesk.entities.Owner;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.services.OwnerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/owners")
public class OwnerController {
    @Autowired
    OwnerService ownerService;

    @PostMapping
    public ResponseEntity<CustomResponse> addOwner(@Valid @RequestBody Owner owner) {
        return ownerService.addOwner(owner);
    }

    @GetMapping
    public ResponseEntity<CustomResponse> getAllOwners() {
        return ownerService.getAllOwners();
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<CustomResponse> getOwner(@PathVariable Long ownerId) {
        return ownerService.getOwner(ownerId);
    }
}