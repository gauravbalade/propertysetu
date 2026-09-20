package com.gaurav.property.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpVerificationRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^(EMAIL|PHONE)$", message = "Channel must be EMAIL or PHONE")
    private String channel;

    @NotBlank
    @Pattern(regexp = "^\\d{4,10}$", message = "OTP must contain only digits")
    private String code;
}
