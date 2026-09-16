package com.gaurav.property.dto;

import com.gaurav.property.enums.VerificationStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationRequest {

    @NotNull
    private Long applicationId;

    @NotNull
    private Long verifiedByUserId;

    @NotNull
    private VerificationStatus status;

    @NotBlank
    private String remarks;
}