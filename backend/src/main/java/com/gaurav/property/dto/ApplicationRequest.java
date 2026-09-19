package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;\nimport jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long propertyId;

    @NotBlank
    @Size(max = 255)
    private String purpose;
}