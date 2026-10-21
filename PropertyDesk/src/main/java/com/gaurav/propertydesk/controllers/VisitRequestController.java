package com.gaurav.propertydesk.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.gaurav.propertydesk.entities.VisitRequest;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.services.VisitRequestService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/visits")
public class VisitRequestController {
    @Autowired
    VisitRequestService visitService;

    @PostMapping
    public ResponseEntity<CustomResponse> addVisitRequest(@Valid @RequestBody VisitRequest visit) {
        return visitService.addVisitRequest(visit);
    }

    @GetMapping
    public ResponseEntity<CustomResponse> getAllVisits() {
        return visitService.getAllVisits();
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<CustomResponse> getVisitsForProperty(@PathVariable Long propertyId) {
        return visitService.getVisitsForProperty(propertyId);
    }

    @PatchMapping("/{visitId}/status")
    public ResponseEntity<CustomResponse> updateStatus(
            @PathVariable Long visitId, @RequestParam String status) {
        return visitService.updateVisitStatus(visitId, status);
    }
}