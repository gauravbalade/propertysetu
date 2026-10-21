package com.gaurav.propertydesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.gaurav.propertydesk.entities.PropertyRecord;
import com.gaurav.propertydesk.entities.VisitRequest;
import com.gaurav.propertydesk.enums.VisitStatus;
import com.gaurav.propertydesk.repositories.PropertyRecordRepository;
import com.gaurav.propertydesk.repositories.VisitRequestRepository;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.response_wrapper.Response;

@Service
public class VisitRequestService {
    @Autowired
    VisitRequestRepository visitRepository;

    @Autowired
    PropertyRecordRepository propertyRepository;

    @Autowired
    Response response;

    public ResponseEntity<CustomResponse> addVisitRequest(VisitRequest visit) {
        if (visit.getProperty() == null || visit.getProperty().getId() == null) {
            return response.send("Please provide a valid property id", null, HttpStatus.BAD_REQUEST);
        }
        PropertyRecord property = propertyRepository.findById(visit.getProperty().getId()).orElse(null);
        if (property == null) {
            return response.send("Property not found", null, HttpStatus.NOT_FOUND);
        }
        visit.setProperty(property);
        visit.setStatus(VisitStatus.REQUESTED);
        return response.send("Visit request created", visitRepository.save(visit), HttpStatus.CREATED);
    }

    public ResponseEntity<CustomResponse> getAllVisits() {
        return response.send("Visit requests retrieved successfully", visitRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<CustomResponse> getVisitsForProperty(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            return response.send("Property not found", null, HttpStatus.NOT_FOUND);
        }
        return response.send("Visit requests retrieved successfully",
                visitRepository.findByPropertyId(propertyId), HttpStatus.OK);
    }

    public ResponseEntity<CustomResponse> updateVisitStatus(Long visitId, String status) {
        VisitRequest visit = visitRepository.findById(visitId).orElse(null);
        if (visit == null) {
            return response.send("Visit request not found", null, HttpStatus.NOT_FOUND);
        }
        try {
            visit.setStatus(VisitStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            return response.send("Status must be REQUESTED, APPROVED, COMPLETED, or CANCELLED",
                    null, HttpStatus.BAD_REQUEST);
        }
        return response.send("Visit request status updated", visitRepository.save(visit), HttpStatus.OK);
    }
}