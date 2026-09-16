package com.gaurav.property.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(nullable = false, length = 80)
    private String entityType;

    @Column(nullable = false, length = 80)
    private String entityId;

    @Column(nullable = false, length = 100)
    private String actorUsername;

    @Column(nullable = false, length = 30)
    private String actorRole;

    @Column(length = 1000)
    private String details;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}
