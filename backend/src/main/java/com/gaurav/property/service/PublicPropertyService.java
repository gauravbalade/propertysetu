package com.gaurav.property.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.PublicPropertyResponse;
import com.gaurav.property.entity.Location;
import com.gaurav.property.entity.Property;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.entity.Verification;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.enums.VerificationStatus;
import com.gaurav.property.repository.LocationRepository;
import com.gaurav.property.repository.PropertyRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;
import com.gaurav.property.repository.VerificationRepository;

@Service
public class PublicPropertyService {

    private final PropertyRepository propertyRepository;
    private final LocationRepository locationRepository;
    private final RegistrationApplicationRepository applicationRepository;
    private final VerificationRepository verificationRepository;

    public PublicPropertyService(
            PropertyRepository propertyRepository,
            LocationRepository locationRepository,
            RegistrationApplicationRepository applicationRepository,
            VerificationRepository verificationRepository) {
        this.propertyRepository = propertyRepository;
        this.locationRepository = locationRepository;
        this.applicationRepository = applicationRepository;
        this.verificationRepository = verificationRepository;
    }

    @Transactional(readOnly = true)
    public List<PublicPropertyResponse> search(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return propertyRepository.findAll().stream()
                .filter(this::isPubliclyVerified)
                .map(this::toPublicResponse)
                .filter(item -> normalized.isEmpty()
                        || contains(item.propertyNumber(), normalized)
                        || contains(item.propertyType(), normalized)
                        || contains(item.city(), normalized)
                        || contains(item.district(), normalized)
                        || contains(item.state(), normalized)
                        || contains(item.ownerDisplayName(), normalized))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicPropertyResponse getById(Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .filter(this::isPubliclyVerified)
                .orElseThrow(() -> new RuntimeException(
                        "Public verified property not found"));
        return toPublicResponse(property);
    }

    private boolean isPubliclyVerified(Property property) {
        List<RegistrationApplication> applications =
                applicationRepository.findByPropertyId(property.getId());
        return applications.stream().anyMatch(application -> {
            if (application.getStatus() != ApplicationStatus.COMPLETED) {
                return false;
            }
            return verificationRepository.findByApplicationId(application.getId())
                    .map(Verification::getStatus)
                    .filter(status -> status == VerificationStatus.VERIFIED)
                    .isPresent();
        });
    }

    private PublicPropertyResponse toPublicResponse(Property property) {
        Location location = locationRepository.findByPropertyId(property.getId()).orElse(null);
        RegistrationApplication application = applicationRepository.findByPropertyId(property.getId())
                .stream()
                .filter(item -> item.getStatus() == ApplicationStatus.COMPLETED)
                .filter(item -> verificationRepository.findByApplicationId(item.getId())
                        .map(Verification::getStatus)
                        .filter(status -> status == VerificationStatus.VERIFIED)
                        .isPresent())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Verified application not found"));

        String maskedReference = "Owner record #" + property.getOwner().getId();

        return new PublicPropertyResponse(
                property.getId(),
                property.getPropertyNumber(),
                property.getPropertyType(),
                property.getArea(),
                property.getDescription(),
                property.getRegistrationStatus(),
                property.getOwner().getName(),
                maskedReference,
                location == null ? null : location.getCity(),
                location == null ? null : location.getDistrict(),
                location == null ? null : location.getState(),
                "VERIFIED",
                application.getApplicationNumber());
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
