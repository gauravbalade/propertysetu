package com.gaurav.property.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gaurav.property.dto.PropertyRequest;
import com.gaurav.property.entity.Owner;
import com.gaurav.property.entity.Property;
import com.gaurav.property.repository.OwnerRepository;
import com.gaurav.property.repository.PropertyRepository;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final AuthorizationService authorizationService;

    public PropertyService(
            PropertyRepository propertyRepository,
            OwnerRepository ownerRepository,
            AuthorizationService authorizationService) {
        this.propertyRepository = propertyRepository;
        this.ownerRepository = ownerRepository;
        this.authorizationService = authorizationService;
    }

    public List<Property> getCurrentProperties() {
        return propertyRepository.findByOwnerUserAccountId(authorizationService.currentUser().getId());
    }

    public Property createProperty(PropertyRequest request) {

        Owner owner = ownerRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        authorizationService.requireOwner(owner.getUserAccount());

        if (propertyRepository.existsByPropertyNumber(request.getPropertyNumber())) {
            throw new RuntimeException("Property number already exists");
        }

        Property property = new Property();
        property.setOwner(owner);
        property.setPropertyNumber(request.getPropertyNumber());
        property.setPropertyType(request.getPropertyType());
        property.setArea(request.getArea());
        property.setDescription(request.getDescription());
        property.setRegistrationStatus("AVAILABLE");
        property.setCreatedAt(LocalDateTime.now());

        return propertyRepository.save(property);
    }
}