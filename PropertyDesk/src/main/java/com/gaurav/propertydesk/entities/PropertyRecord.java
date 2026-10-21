package com.gaurav.propertydesk.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class PropertyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String propertyType;

    @PositiveOrZero
    @Column(nullable = false)
    private Double areaSqFt = 0.0;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String status = "AVAILABLE";

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;
}