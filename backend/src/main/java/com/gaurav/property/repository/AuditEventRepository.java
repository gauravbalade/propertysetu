package com.gaurav.property.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.AuditEvent;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    List<AuditEvent> findByEntityTypeAndEntityIdOrderByOccurredAtDesc(String entityType, String entityId);
}
