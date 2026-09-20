package com.gaurav.property.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.PropertyRequest;
import com.gaurav.property.entity.Property;
import com.gaurav.property.service.PropertyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/me")
    public ResponseEntity<java.util.List<Property>> getCurrentProperties() {
        return ResponseEntity.ok(propertyService.getCurrentProperties());
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyRequest request) {

        return ResponseEntity.ok(propertyService.updateProperty(propertyId, request));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Property> createProperty(
            @Valid @RequestBody PropertyRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(propertyService.createProperty(request));
    }
}