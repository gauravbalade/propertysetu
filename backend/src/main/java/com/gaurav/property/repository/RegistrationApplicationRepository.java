package com.gaurav.property.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.enums.ApplicationStatus;

public interface RegistrationApplicationRepository
        extends JpaRepository<RegistrationApplication, Long> {

    Optional<RegistrationApplication> findByApplicationNumber(String applicationNumber);

    List<RegistrationApplication> findByUserAccountId(Long userId);

    List<RegistrationApplication> findByStatus(ApplicationStatus status);

    List<RegistrationApplication> findByPropertyId(Long propertyId);

    boolean existsByApplicationNumber(String applicationNumber);
}