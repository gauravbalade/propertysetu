package com.gaurav.property.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.DemoOtpRequest;
import com.gaurav.property.dto.ForgotPasswordRequest;
import com.gaurav.property.dto.LoginRequest;
import com.gaurav.property.dto.Msg91VerificationRequest;
import com.gaurav.property.dto.RegisterRequest;
import com.gaurav.property.dto.ResendOtpRequest;
import com.gaurav.property.dto.ResetPasswordRequest;
import com.gaurav.property.dto.UserResponse;
import com.gaurav.property.service.UserAccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response = userAccountService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest request) {

        UserResponse response = userAccountService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/demo-otp/send")
    public ResponseEntity<?> sendDemoOtp(@Valid @RequestBody DemoOtpRequest request) {
        return ResponseEntity.ok(userAccountService.sendDemoOtp(
                request.getUsername(),
                request.getChannel()));
    }

    @PostMapping("/demo-otp/verify")
    public ResponseEntity<UserResponse> verifyDemoOtp(
            @Valid @RequestBody DemoOtpRequest request) {
        return ResponseEntity.ok(userAccountService.verifyDemoOtp(request));
    }

    @PostMapping("/demo-otp/reset-password")
    public ResponseEntity<Void> resetPasswordWithDemoOtp(
            @Valid @RequestBody DemoOtpRequest request) {
        userAccountService.resetPasswordWithDemoOtp(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-msg91-token")
    public ResponseEntity<UserResponse> verifyMsg91Token(
            @Valid @RequestBody Msg91VerificationRequest request) {

        return ResponseEntity.ok(userAccountService.verifyMsg91Otp(request));
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<UserResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request) {

        return ResponseEntity.ok(userAccountService.resendOtp(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        userAccountService.requestPasswordReset(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        userAccountService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
