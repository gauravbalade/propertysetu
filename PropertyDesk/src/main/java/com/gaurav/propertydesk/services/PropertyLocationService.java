package com.gaurav.propertydesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.gaurav.propertydesk.entities.PropertyLocation;
import com.gaurav.propertydesk.entities.PropertyRecord;
import com.gaurav.propertydesk.repositories.PropertyLocationRepository;
import com.gaurav.propertydesk.repositories.PropertyRecordRepository;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.response_wrapper.Response;

@Service
public class PropertyLocationService {
    @Autowired
    PropertyLocationRepository locationRepository;

    @Autowired
    PropertyRecordRepository propertyRepository;

    @Autowired
    Response response;

    public ResponseEntity<CustomResponse> addLocation(PropertyLocation location) {
        if (location.getProperty() == null || location.getProperty().getId() == null) {
            return response.send("Please provide a valid property id", null, HttpStatus.BAD_REQUEST);
        }
        PropertyRecord property = propertyRepository.findById(location.getProperty().getId()).orElse(null);
        if (property == null) {
            return response.send("Property not found", null, HttpStatus.NOT_FOUND);
        }
        if (locationRepository.existsByPropertyId(property.getId())) {
            return response.send("A location already exists for this property", null, HttpStatus.CONFLICT);
        }
        location.setProperty(property);
        return response.send("Property location added", locationRepository.save(location), HttpStatus.CREATED);
    }

    public ResponseEntity<CustomResponse> getLocationByProperty(Long propertyId) {
        return locationRepository.findByPropertyId(propertyId)
                .map(location -> response.send("Location found", location, HttpStatus.OK))
                .orElseGet(() -> response.send("Location not found", null, HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<CustomResponse> getAllLocations() {
        return response.send("Locations retrieved successfully", locationRepository.findAll(), HttpStatus.OK);
    }
}