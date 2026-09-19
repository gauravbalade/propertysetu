package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;\nimport jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest {

    @NotNull
    private Long propertyId;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String district;

    @NotBlank
    private String state;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "PIN code must be 6 digits")
    private String pincode;
}