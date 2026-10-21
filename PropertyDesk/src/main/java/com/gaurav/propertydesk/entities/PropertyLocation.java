package com.gaurav.propertydesk.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class PropertyLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private PropertyRecord property;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String address;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String city;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String district;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String state;

    @NotBlank
    @Pattern(regexp = "[0-9]{6}", message = "Pincode must contain exactly 6 digits")
    @Column(nullable = false, length = 6)
    private String pincode;
}