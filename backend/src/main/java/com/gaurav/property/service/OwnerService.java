package com.gaurav.property.service;

import org.springframework.stereotype.Service;

import java.util.Optional;

import com.gaurav.property.dto.OwnerRequest;
import com.gaurav.property.entity.Owner;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.repository.OwnerRepository;
import com.gaurav.property.repository.UserAccountRepository;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuthorizationService authorizationService;

    public OwnerService(
            OwnerRepository ownerRepository,
            UserAccountRepository userAccountRepository,
            AuthorizationService authorizationService) {
        this.ownerRepository = ownerRepository;
        this.userAccountRepository = userAccountRepository;
        this.authorizationService = authorizationService;
    }

    public Optional<Owner> getCurrentOwner() {
        return ownerRepository.findByUserAccountId(authorizationService.currentUser().getId());
    }

    public Owner createOwner(OwnerRequest request) {

        UserAccount user = userAccountRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        authorizationService.requireOwner(user);

        if (ownerRepository.findByUserAccountId(request.getUserId()).isPresent()) {
            throw new RuntimeException("Owner profile already exists");
        }

        if (ownerRepository.existsByIdentityNumber(request.getIdentityNumber())) {
            throw new RuntimeException("Identity number already exists");
        }

        Owner owner = new Owner();
        owner.setUserAccount(user);
        owner.setName(request.getName());
        owner.setAddress(request.getAddress());
        owner.setPhone(request.getPhone());
        owner.setIdentityNumber(request.getIdentityNumber());

        return ownerRepository.save(owner);
    }
}