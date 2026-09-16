package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentVerificationRequest {

    @NotNull
    private Long paymentId;

    @NotBlank
    private String paymentReference;

    @NotNull
    private Boolean successful;
}