package com.gaurav.propertydesk.entities;

import java.time.LocalDate;
import com.gaurav.propertydesk.enums.VisitStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class VisitRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private PropertyRecord property;

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDate visitDate;

    @Size(max = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisitStatus status = VisitStatus.REQUESTED;
}