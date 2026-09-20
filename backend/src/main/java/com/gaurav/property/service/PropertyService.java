package com.gaurav.property.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.gaurav.property.dto.PropertyRequest;
import com.gaurav.property.entity.Owner;
import com.gaurav.property.entity.Property;
import com.gaurav.property.repository.OwnerRepository;
import com.gaurav.property.repository.PropertyRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;
import com.gaurav.property.enums.ApplicationStatus;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OwnerRepository ownerRepository;
    private final AuthorizationService authorizationService;
    private final RegistrationApplicationRepository applicationRepository;
    private final LocationService locationService;

    public PropertyService(
            PropertyRepository propertyRepository,
            OwnerRepository ownerRepository,
            AuthorizationService authorizationService,
            RegistrationApplicationRepository applicationRepository,
            LocationService locationService) {
        this.propertyRepository = propertyRepository;
        this.ownerRepository = ownerRepository;
        this.authorizationService = authorizationService;
        this.applicationRepository = applicationRepository;
        this.locationService = locationService;
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

    public Property updateProperty(Long propertyId, PropertyRequest request) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        authorizationService.requireOwner(property.getOwner().getUserAccount());

        if (!property.getPropertyNumber().equals(request.getPropertyNumber())
                && propertyRepository.existsByPropertyNumber(request.getPropertyNumber())) {
            throw new RuntimeException("Property number already exists");
        }

        if (applicationRepository.findByPropertyId(propertyId).stream()
                .anyMatch(application -> application.getStatus() != ApplicationStatus.DRAFT)) {
            throw new RuntimeException("This property cannot be edited after its application leaves DRAFT status.");
        }

        property.setPropertyNumber(request.getPropertyNumber().trim());
        property.setPropertyType(request.getPropertyType().trim());
        property.setArea(request.getArea());
        property.setDescription(request.getDescription());

        Property saved = propertyRepository.save(property);
        return saved;
    }

    public void deleteProperty(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        authorizationService.requireOwner(property.getOwner().getUserAccount());

        if (applicationRepository.findByPropertyId(propertyId).stream().findAny().isPresent()) {
            throw new RuntimeException("A property with an application record cannot be deleted.");
        }

        if (locationService.getLocation(propertyId) != null) {
            throw new RuntimeException("Remove the property location before deleting this property.");
        }

        propertyRepository.delete(property);
    }
}