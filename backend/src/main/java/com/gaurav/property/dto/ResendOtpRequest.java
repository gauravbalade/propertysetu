package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendOtpRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^(EMAIL|PHONE)$", message = "Channel must be EMAIL or PHONE")
    private String channel;
}
