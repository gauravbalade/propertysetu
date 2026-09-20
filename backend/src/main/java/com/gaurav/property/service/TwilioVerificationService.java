package com.gaurav.property.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

@Service
public class TwilioVerificationService {

    private final String accountSid;
    private final String authToken;
    private final String serviceSid;

    public TwilioVerificationService(
            @Value("${TWILIO_ACCOUNT_SID:}") String accountSid,
            @Value("${TWILIO_AUTH_TOKEN:}") String authToken,
            @Value("${TWILIO_VERIFY_SERVICE_SID:}") String serviceSid) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.serviceSid = serviceSid;
    }

    public boolean isConfigured() {
        return !accountSid.isBlank() && !authToken.isBlank() && !serviceSid.isBlank();
    }

    public String send(String destination, String channel) {
        requireConfigured();

        try {
            Twilio.init(accountSid, authToken);
            Verification verification = Verification.creator(serviceSid)
                    .setTo(destination)
                    .setChannel(channel.toLowerCase())
                    .create();

            return verification.getSid();
        } catch (ApiException ex) {
            throw new RuntimeException("Unable to send the verification code. Please try again.");
        }
    }

    public boolean verify(String destination, String code) {
        requireConfigured();

        try {
            Twilio.init(accountSid, authToken);
            VerificationCheck check = VerificationCheck.creator(serviceSid)
                    .setTo(destination)
                    .setCode(code)
                    .create();

            return "approved".equalsIgnoreCase(check.getStatus());
        } catch (ApiException ex) {
            throw new RuntimeException("Unable to verify the code. Please request a new code and try again.");
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new RuntimeException(
                    "Account verification is temporarily unavailable because the verification provider is not configured.");
        }
    }
}
