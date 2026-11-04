package com.gaurav.property.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gaurav.property.entity.RegistrationApplication;
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
        UserAccount user = userAccountRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        if (!Boolean.TRUE.equals(user.getActive())) {
            SecurityContextHolder.clearContext();
            throw new RuntimeException("Your account is inactive. Contact the administrator.");
        }
        return user;
    }

    public void requireOfficer() {
        if (currentUser().getRole() != UserRole.OFFICER) {
            throw new RuntimeException("Officer access is required");
        }
    }

    public UserAccount requireAdmin() {
        UserAccount user = currentUser();
        if (user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Admin access is required");
        }
        return user;
    }

    public boolean isOfficer() {
        return currentUser().getRole() == UserRole.OFFICER;
    }

    public boolean isAdmin() {
        return currentUser().getRole() == UserRole.ADMIN;
    }

    /** Applicant-only ownership check. Staff access is checked separately per case. */
    public void requireOwner(UserAccount owner) {
        UserAccount actor = currentUser();
        if (actor.getRole() != UserRole.APPLICANT || owner == null
                || owner.getId() == null || !owner.getId().equals(actor.getId())) {
            throw new RuntimeException("You are not allowed to access this applicant resource");
        }
    }

    public void requireAssignedOfficer(RegistrationApplication application) {
        UserAccount actor = currentUser();
        if (actor.getRole() != UserRole.OFFICER) {
            throw new RuntimeException("Only an officer can perform this review action");
        }
        if (application == null || application.getAssignedOfficer() == null
                || !actor.getId().equals(application.getAssignedOfficer().getId())) {
            throw new RuntimeException("This application is not assigned to your officer account");
        }
    }

    public void requireCanReadApplication(RegistrationApplication application) {
        UserAccount actor = currentUser();
        if (application == null) {
            throw new RuntimeException("Application not found");
        }
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (actor.getRole() == UserRole.OFFICER) {
            if (application.getAssignedOfficer() != null
                    && actor.getId().equals(application.getAssignedOfficer().getId())) {
                return;
            }
            throw new RuntimeException("This application is not assigned to your officer account");
        }
        if (application.getUserAccount() == null
                || !actor.getId().equals(application.getUserAccount().getId())) {
            throw new RuntimeException("You are not allowed to access this application");
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
