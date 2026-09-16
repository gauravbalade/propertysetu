package com.gaurav.property.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.repository.UserAccountRepository;

@Service
public class AuthorizationService {

    private final UserAccountRepository userAccountRepository;

    public AuthorizationService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserAccount currentUser() {
        Authentication authentication = authentication();
        return userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    public void requireOfficer() {
        Authentication authentication = authentication();
        boolean officer = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.OFFICER.name())
                        || a.getAuthority().equals("ROLE_" + UserRole.ADMIN.name()));
        if (!officer) {
            throw new RuntimeException("Officer access is required");
        }
    }

    public boolean isOfficer() {
        Authentication authentication = authentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.OFFICER.name())
                        || a.getAuthority().equals("ROLE_" + UserRole.ADMIN.name()));
    }

    public void requireOwner(UserAccount owner) {
        if (owner == null || (!isOfficer() && !owner.getUsername().equals(currentUser().getUsername()))) {
            throw new RuntimeException("You are not allowed to access this resource");
        }
    }

    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Authentication is required");
        }
        return authentication;
    }
}
