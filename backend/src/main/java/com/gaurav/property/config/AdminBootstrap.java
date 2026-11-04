package com.gaurav.property.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.repository.UserAccountRepository;

/**
 * Optional one-time administrator bootstrap for a fresh local database.
 *
 * It is intentionally disabled unless all three environment variables are set.
 * Existing accounts are never modified and their passwords are never reset.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_BOOTSTRAP_USERNAME:}") String username,
            @Value("${ADMIN_BOOTSTRAP_EMAIL:}") String email,
            @Value("${ADMIN_BOOTSTRAP_PASSWORD:}") String password) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username == null ? "" : username.trim();
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() && email.isBlank() && password.isBlank()) {
            return;
        }

        if (username.isBlank() || email.isBlank() || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin bootstrap requires ADMIN_BOOTSTRAP_USERNAME, ADMIN_BOOTSTRAP_EMAIL, "
                            + "and an ADMIN_BOOTSTRAP_PASSWORD of at least 12 characters.");
        }

        UserAccount existing = userAccountRepository.findByUsername(username).orElse(null);
        if (existing != null) {
            if (existing.getRole() != UserRole.ADMIN) {
                throw new IllegalStateException(
                        "The configured admin bootstrap username already belongs to a non-admin account. "
                                + "Choose a different username; no account was changed.");
            }
            log.info("Admin bootstrap skipped: the configured administrator account already exists.");
            return;
        }

        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "The configured admin bootstrap email is already in use. "
                            + "Choose the email belonging to your intended administrator.");
        }

        UserAccount admin = UserAccount.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .active(true)
                .emailVerified(true)
                .phoneVerified(false)
                .build();

        userAccountRepository.save(admin);
        log.info("Initial PropertySetu administrator created from local bootstrap settings. "
                + "Remove ADMIN_BOOTSTRAP_PASSWORD from backend/.env.local after first startup.");
    }
}
