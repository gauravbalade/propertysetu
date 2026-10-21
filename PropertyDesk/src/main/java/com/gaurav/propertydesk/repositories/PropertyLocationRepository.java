package com.gaurav.propertydesk.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gaurav.propertydesk.entities.PropertyLocation;

@Repository
public interface PropertyLocationRepository extends JpaRepository<PropertyLocation, Long> {
    Optional<PropertyLocation> findByPropertyId(Long propertyId);
    boolean existsByPropertyId(Long propertyId);
}