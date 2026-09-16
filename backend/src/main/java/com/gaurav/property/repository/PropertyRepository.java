package com.gaurav.property.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Property;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Optional<Property> findByPropertyNumber(String propertyNumber);

    List<Property> findByOwnerId(Long ownerId);

    boolean existsByPropertyNumber(String propertyNumber);
}