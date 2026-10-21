package com.gaurav.propertydesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.gaurav.propertydesk.entities.Owner;
import com.gaurav.propertydesk.entities.PropertyRecord;
import com.gaurav.propertydesk.repositories.OwnerRepository;
import com.gaurav.propertydesk.repositories.PropertyRecordRepository;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.response_wrapper.Response;

@Service
public class PropertyService {
    @Autowired
    PropertyRecordRepository propertyRepository;

    @Autowired
    OwnerRepository ownerRepository;

    @Autowired
    Response response;

    public ResponseEntity<CustomResponse> addProperty(PropertyRecord property) {
        if (property.getOwner() == null || property.getOwner().getId() == null) {
            return response.send("Please provide a valid owner id", null, HttpStatus.BAD_REQUEST);
        }
        Owner owner = ownerRepository.findById(property.getOwner().getId()).orElse(null);
        if (owner == null) {
            return response.send("Owner not found", null, HttpStatus.NOT_FOUND);
        }
        property.setOwner(owner);
        PropertyRecord savedProperty = propertyRepository.save(property);
        return response.send("Property registered successfully", savedProperty, HttpStatus.CREATED);
    }

    public ResponseEntity<CustomResponse> getAllProperties() {
        return response.send("Properties retrieved successfully", propertyRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<CustomResponse> getProperty(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .map(property -> response.send("Property found", property, HttpStatus.OK))
                .orElseGet(() -> response.send("Property not found", null, HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<CustomResponse> getPropertiesByOwner(Long ownerId) {
        if (!ownerRepository.existsById(ownerId)) {
            return response.send("Owner not found", null, HttpStatus.NOT_FOUND);
        }
        return response.send("Owner properties retrieved successfully",
                propertyRepository.findByOwnerId(ownerId), HttpStatus.OK);
    }

    public ResponseEntity<CustomResponse> updatePropertyStatus(Long propertyId, String status) {
        PropertyRecord property = propertyRepository.findById(propertyId).orElse(null);
        if (property == null) {
            return response.send("Property not found", null, HttpStatus.NOT_FOUND);
        }
        if (!status.equalsIgnoreCase("AVAILABLE") && !status.equalsIgnoreCase("UNDER_REVIEW")
                && !status.equalsIgnoreCase("REGISTERED")) {
            return response.send("Status must be AVAILABLE, UNDER_REVIEW, or REGISTERED", null, HttpStatus.BAD_REQUEST);
        }
        property.setStatus(status.toUpperCase());
        return response.send("Property status updated", propertyRepository.save(property), HttpStatus.OK);
    }

    public ResponseEntity<CustomResponse> deleteProperty(Long propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            return response.send("Property not found", null, HttpStatus.NOT_FOUND);
        }
        propertyRepository.deleteById(propertyId);
        return response.send("Property deleted successfully", null, HttpStatus.OK);
    }
}