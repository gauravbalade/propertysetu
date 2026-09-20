package com.gaurav.property.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.AdminUserCreateRequest;
import com.gaurav.property.dto.AdminUserUpdateRequest;
import com.gaurav.property.dto.UserResponse;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.repository.UserAccountRepository;

@Service
public class AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    public AdminUserService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthorizationService authorizationService,
            AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        authorizationService.requireAdmin();
        return userAccountRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        UserAccount::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(user -> toResponse(user))
                .toList();
    }

    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {
        UserAccount admin = authorizationService.requireAdmin();

        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? "" : request.getPhone().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (username.isBlank() || email.isBlank() || password.length() < 6 || password.length() > 100) {
            throw new RuntimeException("Username, email and a 6-100 character password are required.");
        }
        if (userAccountRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists.");
        }
        if (userAccountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists.");
        }

        UserRole role = request.getRole() == null ? UserRole.OFFICER : request.getRole();
        UserAccount user = UserAccount.builder()
                .username(username)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .emailVerified(true)
                .phoneVerified(true)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        auditService.record("ADMIN_USER_CREATED", "USER", saved.getId(), admin,
                "Admin created account " + saved.getUsername() + " with role " + saved.getRole().name());
        return toResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        UserAccount admin = authorizationService.requireAdmin();
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (admin.getId().equals(user.getId())) {
            if (Boolean.FALSE.equals(request.getActive())
                    || (request.getRole() != null && request.getRole() != UserRole.ADMIN)) {
                throw new RuntimeException("You cannot remove ADMIN access or deactivate your own account.");
            }
        }

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
        if (request.getEmailVerified() != null) {
            user.setEmailVerified(request.getEmailVerified());
        }
        if (request.getPhoneVerified() != null) {
            user.setPhoneVerified(request.getPhoneVerified());
        }

        UserAccount saved = userAccountRepository.save(user);
        auditService.record("ADMIN_USER_UPDATED", "USER", saved.getId(), admin,
                "Admin updated role/status/verification flags for " + saved.getUsername());
        return toResponse(saved);
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                null,
                false,
                Boolean.TRUE.equals(user.getEmailVerified()),
                Boolean.TRUE.equals(user.getPhoneVerified()),
                Boolean.TRUE.equals(user.getActive()),
                user.getCreatedAt());
    }
}
