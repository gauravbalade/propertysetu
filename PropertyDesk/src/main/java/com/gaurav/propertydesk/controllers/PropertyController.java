package com.gaurav.propertydesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gaurav.propertydesk.entities.PropertyRecord;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.services.PropertyService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {
    @Autowired
    PropertyService propertyService;

    @PostMapping
    public ResponseEntity<CustomResponse> addProperty(@Valid @RequestBody PropertyRecord property) {
        return propertyService.addProperty(property);
    }

    @GetMapping
    public ResponseEntity<CustomResponse> getAllProperties() {
        return propertyService.getAllProperties();
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<CustomResponse> getProperty(@PathVariable Long propertyId) {
        return propertyService.getProperty(propertyId);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<CustomResponse> getPropertiesByOwner(@PathVariable Long ownerId) {
        return propertyService.getPropertiesByOwner(ownerId);
    }

    @PatchMapping("/{propertyId}/status")
    public ResponseEntity<CustomResponse> updateStatus(
            @PathVariable Long propertyId, @RequestParam String status) {
        return propertyService.updatePropertyStatus(propertyId, status);
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<CustomResponse> deleteProperty(@PathVariable Long propertyId) {
        return propertyService.deleteProperty(propertyId);
    }
}