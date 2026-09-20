package com.gaurav.property.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.LocationRequest;
import com.gaurav.property.entity.Location;
import com.gaurav.property.service.LocationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<Location> getLocation(@PathVariable Long propertyId) {
        Location location = locationService.getLocation(propertyId);
        return location == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(location);
    }

    @PutMapping("/{locationId}")
    public ResponseEntity<Location> updateLocation(
            @PathVariable Long locationId,
            @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(locationId, request));
    }

    @DeleteMapping("/{locationId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long locationId) {
        locationService.deleteLocation(locationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<Location> createLocation(
            @Valid @RequestBody LocationRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationService.createLocation(request));
    }
}