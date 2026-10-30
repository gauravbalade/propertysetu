package com.gaurav.property.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.PublicPropertyResponse;
import com.gaurav.property.service.PublicPropertyService;

@RestController
@RequestMapping("/api/public/properties")
public class PublicPropertyController {

    private final PublicPropertyService publicPropertyService;

    public PublicPropertyController(PublicPropertyService publicPropertyService) {
        this.publicPropertyService = publicPropertyService;
    }

    @GetMapping
    public ResponseEntity<List<PublicPropertyResponse>> search(
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(publicPropertyService.search(query));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PublicPropertyResponse> getById(
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(publicPropertyService.getById(propertyId));
    }
}
