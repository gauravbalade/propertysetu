package com.gaurav.property.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Document;
import com.gaurav.property.enums.DocumentStatus;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByApplicationId(Long applicationId);

    Optional<Document> findFirstByApplicationIdAndDocumentType(
            Long applicationId, String documentType);

    List<Document> findByStatus(DocumentStatus status);
}