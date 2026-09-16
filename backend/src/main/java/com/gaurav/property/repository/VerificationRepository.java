package com.gaurav.property.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Verification;

public interface VerificationRepository extends JpaRepository<Verification, Long> {

    Optional<Verification> findByApplicationId(Long applicationId);
}