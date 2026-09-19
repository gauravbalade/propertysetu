package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;\nimport jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OwnerRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String address;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be a 10-digit mobile number")
    @Size(max = 20)
    private String phone;

    @NotBlank
    @Size(max = 30)
    private String identityNumber;
}