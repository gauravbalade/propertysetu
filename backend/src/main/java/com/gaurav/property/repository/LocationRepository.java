package com.gaurav.property.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByPropertyId(Long propertyId);
}