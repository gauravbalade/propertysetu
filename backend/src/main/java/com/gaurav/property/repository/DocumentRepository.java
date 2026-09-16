package com.gaurav.property.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Document;
import com.gaurav.property.enums.DocumentStatus;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByApplicationId(Long applicationId);

    List<Document> findByStatus(DocumentStatus status);
}