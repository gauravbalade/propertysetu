package com.gaurav.propertydesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gaurav.propertydesk.entities.PropertyLocation;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.services.PropertyLocationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/locations")
public class PropertyLocationController {
    @Autowired
    PropertyLocationService locationService;

    @PostMapping
    public ResponseEntity<CustomResponse> addLocation(@Valid @RequestBody PropertyLocation location) {
        return locationService.addLocation(location);
    }

    @GetMapping
    public ResponseEntity<CustomResponse> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<CustomResponse> getLocationByProperty(@PathVariable Long propertyId) {
        return locationService.getLocationByProperty(propertyId);
    }
}