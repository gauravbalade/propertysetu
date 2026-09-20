package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DemoOtpRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String channel;

    private String code;

    private String newPassword;
}
