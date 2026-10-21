package com.gaurav.propertydesk.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.gaurav.propertydesk.entities.Owner;
import com.gaurav.propertydesk.repositories.OwnerRepository;
import com.gaurav.propertydesk.response_wrapper.CustomResponse;
import com.gaurav.propertydesk.response_wrapper.Response;

@Service
public class OwnerService {
    @Autowired
    OwnerRepository ownerRepository;

    @Autowired
    Response response;

    public ResponseEntity<CustomResponse> addOwner(Owner owner) {
        if (ownerRepository.existsByEmail(owner.getEmail())) {
            return response.send("An owner with this email already exists", null, HttpStatus.CONFLICT);
        }
        Owner savedOwner = ownerRepository.save(owner);
        return response.send("Owner registered successfully", savedOwner, HttpStatus.CREATED);
    }

    public ResponseEntity<CustomResponse> getAllOwners() {
        return response.send("Owners retrieved successfully", ownerRepository.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<CustomResponse> getOwner(Long ownerId) {
        return ownerRepository.findById(ownerId)
                .map(owner -> response.send("Owner found", owner, HttpStatus.OK))
                .orElseGet(() -> response.send("Owner not found", null, HttpStatus.NOT_FOUND));
    }
}