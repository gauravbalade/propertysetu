package com.gaurav.property.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Owner;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByUserAccountId(Long userId);

    boolean existsByIdentityNumber(String identityNumber);
}