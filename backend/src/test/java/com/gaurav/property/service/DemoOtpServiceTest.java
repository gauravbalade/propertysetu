package com.gaurav.property.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DemoOtpServiceTest {

    @Test
    void demoOtpCanBeVerifiedOnceAndIsCaseInsensitiveForChannel() {
        DemoOtpService service = new DemoOtpService(true);
        String code = service.issue("ApplicantOne", "email");

        assertFalse(service.verify("ApplicantOne", "EMAIL", "000000".equals(code) ? "000001" : "000000")
                && !"000000".equals(code));
        assertTrue(service.verify("applicantone", "email", code));
        assertFalse(service.verify("ApplicantOne", "EMAIL", code));
    }

    @Test
    void demoOtpRejectsUnsupportedChannels() {
        DemoOtpService service = new DemoOtpService(true);

        assertThrows(RuntimeException.class, () -> service.issue("ApplicantOne", "SMS"));
    }

    @Test
    void demoOtpIsDisabledByDefault() {
        DemoOtpService service = new DemoOtpService(false);

        assertThrows(RuntimeException.class, () -> service.issue("ApplicantOne", "EMAIL"));
        assertThrows(RuntimeException.class, () -> service.verify("ApplicantOne", "EMAIL", "123456"));
    }
}
