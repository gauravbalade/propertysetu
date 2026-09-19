package com.gaurav.property.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;\nimport jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyRequest {

    @NotNull
    private Long ownerId;

    @NotBlank
    @Size(max = 50)
    private String propertyNumber;

    @NotBlank
    @Size(max = 50)
    private String propertyType;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal area;

    @Size(max = 1000)
    private String description;
}