package com.gaurav.property.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.ForgotPasswordRequest;
import com.gaurav.property.dto.LoginRequest;
import com.gaurav.property.dto.OtpVerificationRequest;
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
    private final TwilioVerificationService twilioVerificationService;
    private final AuditService auditService;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            TwilioVerificationService twilioVerificationService,
            AuditService auditService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.twilioVerificationService = twilioVerificationService;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (!twilioVerificationService.isConfigured()) {
            throw new RuntimeException(
                    "Account verification is not configured yet. Please try again later.");
        }

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
        sendVerificationCodes(savedUser);

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
    public UserResponse verifyOtp(OtpVerificationRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String channel = request.getChannel().trim().toUpperCase();
        String destination;
        String verificationSid;

        if ("EMAIL".equals(channel)) {
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                return toResponse(user, null, !isFullyVerified(user));
            }
            destination = user.getEmail();
            verificationSid = user.getEmailVerificationSid();
        } else {
            if (Boolean.TRUE.equals(user.getPhoneVerified())) {
                return toResponse(user, null, !isFullyVerified(user));
            }
            destination = normalizeIndianPhone(user.getPhone());
            verificationSid = user.getPhoneVerificationSid();
        }

        if (verificationSid == null || verificationSid.isBlank()) {
            throw new RuntimeException("No active OTP was found. Request a new verification code.");
        }

        boolean approved = twilioVerificationService.verify(destination, request.getCode().trim());
        if (!approved) {
            throw new RuntimeException("Incorrect or expired OTP.");
        }

        if ("EMAIL".equals(channel)) {
            user.setEmailVerified(true);
            user.setEmailVerificationSid(null);
        } else {
            user.setPhoneVerified(true);
            user.setPhoneVerificationSid(null);
        }

        if (isFullyVerified(user)) {
            user.setActive(true);
        }

        UserAccount saved = userAccountRepository.save(user);

        auditService.record("ACCOUNT_VERIFIED_" + channel, "USER", saved.getId(),
                saved, channel + " OTP verified");

        return toResponse(saved, null, !isFullyVerified(saved));
    }

    @Transactional
    public UserResponse resendOtp(ResendOtpRequest request) {
        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        String channel = request.getChannel().trim().toUpperCase();

        if ("EMAIL".equals(channel) && Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("Email is already verified.");
        }

        if ("PHONE".equals(channel) && Boolean.TRUE.equals(user.getPhoneVerified())) {
            throw new RuntimeException("Mobile number is already verified.");
        }

        String sid;
        if ("EMAIL".equals(channel)) {
            sid = twilioVerificationService.send(user.getEmail(), "email");
            user.setEmailVerificationSid(sid);
        } else {
            sid = twilioVerificationService.send(normalizeIndianPhone(user.getPhone()), "sms");
            user.setPhoneVerificationSid(sid);
        }

        UserAccount saved = userAccountRepository.save(user);
        auditService.record("OTP_RESENT_" + channel, "USER", saved.getId(),
                saved, channel + " verification code resent");

        return toResponse(saved, null, true);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        userAccountRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .ifPresent(user -> {
                    if (!twilioVerificationService.isConfigured()) {
                        throw new RuntimeException(
                                "Password recovery is temporarily unavailable. Please try again later.");
                    }

                    String sid = twilioVerificationService.send(user.getEmail(), "email");
                    user.setPasswordResetVerificationSid(sid);
                    userAccountRepository.save(user);

                    auditService.record("PASSWORD_RESET_REQUESTED", "USER", user.getId(),
                            user, "Password reset verification requested");
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Password reset request is invalid or expired"));

        if (user.getPasswordResetVerificationSid() == null
                || user.getPasswordResetVerificationSid().isBlank()) {
            throw new RuntimeException("Password reset request is invalid or expired");
        }

        boolean approved = twilioVerificationService.verify(
                user.getEmail(), request.getCode().trim());

        if (!approved) {
            throw new RuntimeException("Incorrect or expired password reset OTP.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetVerificationSid(null);
        userAccountRepository.save(user);

        auditService.record("PASSWORD_RESET_COMPLETED", "USER", user.getId(),
                user, "Password reset completed after email verification");
    }

    private void sendVerificationCodes(UserAccount user) {
        String emailSid = twilioVerificationService.send(user.getEmail(), "email");
        user.setEmailVerificationSid(emailSid);

        String phoneSid = twilioVerificationService.send(
                normalizeIndianPhone(user.getPhone()), "sms");
        user.setPhoneVerificationSid(phoneSid);

        userAccountRepository.save(user);
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
