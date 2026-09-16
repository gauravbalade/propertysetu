package com.gaurav.property.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentOrderRequest {

    @NotNull
    private Long applicationId;

    @NotNull
    @DecimalMin(value = "1.00")
    private BigDecimal amount;
}