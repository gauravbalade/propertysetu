package com.gaurav.property.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.ForgotPasswordRequest;
import com.gaurav.property.dto.LoginRequest;
import com.gaurav.property.dto.Msg91VerificationRequest;
import com.gaurav.property.dto.RegisterRequest;
import com.gaurav.property.dto.ResendOtpRequest;
import com.gaurav.property.dto.ResetPasswordRequest;
import com.gaurav.property.dto.UserResponse;
import com.gaurav.property.entity.UserAccount;
import com.gaurav.property.enums.UserRole;
import com.gaurav.property.repository.UserAccountRepository;
import com.gaurav.property.security.JwtService;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Msg91WidgetService msg91WidgetService;
    private final AuditService auditService;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            Msg91WidgetService msg91WidgetService,
            AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.twilioVerificationService = twilioVerificationService;
        this.msg91WidgetService = msg91WidgetService;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists. If this is your account, use OTP verification.");
        }

        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists. If this is your account, use OTP verification.");
        }

        UserAccount user = UserAccount.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone().trim())
                .role(UserRole.APPLICANT)
                .active(false)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        UserAccount savedUser = userAccountRepository.save(user);

        auditService.record("ACCOUNT_CREATED_PENDING_VERIFICATION", "USER", savedUser.getId(),
                savedUser, "Account created; email and mobile verification required");

        return toResponse(savedUser, null, true);
    }

    public UserResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (user.getRole() == UserRole.APPLICANT
                && (!Boolean.TRUE.equals(user.getEmailVerified())
                || !Boolean.TRUE.equals(user.getPhoneVerified()))) {
            throw new RuntimeException(
                    "Account verification is incomplete. Verify your email and mobile OTP before logging in.");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new RuntimeException("Account is inactive");
        }

        // Officer/admin accounts are provisioned separately and are not blocked by
        // legacy applicant contact-verification fields.

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return toResponse(user, jwtService.generateToken(user), false);
    }

    @Transactional
    public UserResponse verifyMsg91Otp(Msg91VerificationRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String channel = request.getChannel().trim().toUpperCase();
        if (!"EMAIL".equals(channel) && !"PHONE".equals(channel)) {
            throw new RuntimeException("Verification channel must be EMAIL or PHONE.");
        }

        if ("EMAIL".equals(channel) && Boolean.TRUE.equals(user.getEmailVerified())) {
            return toResponse(user, null, !isFullyVerified(user));
        }
        if ("PHONE".equals(channel) && Boolean.TRUE.equals(user.getPhoneVerified())) {
            return toResponse(user, null, !isFullyVerified(user));
        }

        String expectedIdentifier = "EMAIL".equals(channel)
                ? user.getEmail()
                : normalizeIndianPhone(user.getPhone());

        com.fasterxml.jackson.databind.JsonNode verification =
                msg91WidgetService.verifyAccessToken(request.getAccessToken());

        if (!msg91WidgetService.containsIdentifier(verification, expectedIdentifier)) {
            throw new RuntimeException(
                    "The verified contact does not match this PropertySetu account.");
        }

        if ("EMAIL".equals(channel)) {
            user.setEmailVerified(true);
        } else {
            user.setPhoneVerified(true);
        }

        if (isFullyVerified(user)) {
            user.setActive(true);
        }

        UserAccount saved = userAccountRepository.save(user);

        auditService.record("ACCOUNT_VERIFIED_" + channel, "USER", saved.getId(),
                saved, channel + " OTP verified through MSG91");

        return toResponse(saved, null, !isFullyVerified(saved));
    }

    @Transactional
    public UserResponse resendOtp(ResendOtpRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String channel = request.getChannel().trim().toUpperCase();
        if (!"EMAIL".equals(channel) && !"PHONE".equals(channel)) {
            throw new RuntimeException("Verification channel must be EMAIL or PHONE.");
        }
        if ("EMAIL".equals(channel) && Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified.");
        }
        if ("PHONE".equals(channel) && Boolean.TRUE.equals(user.getPhoneVerified())) {
            throw new RuntimeException("Mobile number is already verified.");
        }

        // OTP delivery is performed by the MSG91 Widget on the client. This endpoint
        // remains for API compatibility; the UI uses MSG91 retryOtp instead.
        throw new RuntimeException("Use the MSG91 verification screen to resend the OTP.");
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        // Keep this response deliberately non-enumerating. The frontend then starts
        // the MSG91 email OTP flow using the same widget as account verification.
        String email = request.getEmail().trim().toLowerCase();
        userAccountRepository.findByEmail(email).ifPresent(user ->
                auditService.record("PASSWORD_RESET_REQUESTED", "USER", user.getId(),
                        user, "Password reset verification requested through MSG91"));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Password reset request is invalid or expired"));

        com.fasterxml.jackson.databind.JsonNode verification =
                msg91WidgetService.verifyAccessToken(request.getAccessToken());

        if (!msg91WidgetService.containsIdentifier(verification, user.getEmail())) {
            throw new RuntimeException("The verified email does not match this PropertySetu account.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetVerificationSid(null);
        userAccountRepository.save(user);

        auditService.record("PASSWORD_RESET_COMPLETED", "USER", user.getId(),
                user, "Password reset completed after MSG91 email verification");
    }

    private boolean isFullyVerified(UserAccount user) {
        return Boolean.TRUE.equals(user.getEmailVerified())
                && Boolean.TRUE.equals(user.getPhoneVerified());
    }

    private String normalizeIndianPhone(String phone) {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "+91" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            return "+" + digits;
        }
        throw new RuntimeException("Mobile number must be a valid 10-digit Indian mobile number.");
    }

    private UserResponse toResponse(UserAccount user, String token, boolean verificationRequired) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                token,
                verificationRequired,
                Boolean.TRUE.equals(user.getEmailVerified()),
                Boolean.TRUE.equals(user.getPhoneVerified()));
    }
}
