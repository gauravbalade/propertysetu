package com.gaurav.property.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyRequest {

    @NotNull
    private Long ownerId;

    @NotBlank
    private String propertyNumber;

    @NotBlank
    private String propertyType;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal area;

    private String description;
}