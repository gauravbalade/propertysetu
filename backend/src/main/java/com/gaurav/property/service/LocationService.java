package com.gaurav.property.service;

import org.springframework.stereotype.Service;

import com.gaurav.property.dto.LocationRequest;
import com.gaurav.property.entity.Location;
import com.gaurav.property.entity.Property;
import com.gaurav.property.repository.LocationRepository;
import com.gaurav.property.repository.PropertyRepository;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final PropertyRepository propertyRepository;
    private final AuthorizationService authorizationService;

    public LocationService(
            LocationRepository locationRepository,
            PropertyRepository propertyRepository,
            AuthorizationService authorizationService) {
        this.locationRepository = locationRepository;
        this.propertyRepository = propertyRepository;
        this.authorizationService = authorizationService;
    }

    public Location getLocation(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        authorizationService.requireOwner(property.getOwner().getUserAccount());
        return locationRepository.findByPropertyId(propertyId).orElse(null);
    }

    public Location updateLocation(Long locationId, LocationRequest request) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        authorizationService.requireOwner(location.getProperty().getOwner().getUserAccount());

        location.setAddress(request.getAddress().trim());
        location.setCity(request.getCity().trim());
        location.setDistrict(request.getDistrict().trim());
        location.setState(request.getState().trim());
        location.setPincode(request.getPincode().trim());

        return locationRepository.save(location);
    }

    public void deleteLocation(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        authorizationService.requireOwner(location.getProperty().getOwner().getUserAccount());

        locationRepository.delete(location);
    }

    public Location createLocation(LocationRequest request) {

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Property not found"));
        authorizationService.requireOwner(property.getOwner().getUserAccount());

        if (locationRepository.findByPropertyId(request.getPropertyId()).isPresent()) {
            throw new RuntimeException("Location already exists for this property");
        }

        Location location = new Location();
        location.setProperty(property);
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());
        location.setDistrict(request.getDistrict());
        location.setState(request.getState());
        location.setPincode(request.getPincode());

        return locationRepository.save(location);
    }
}