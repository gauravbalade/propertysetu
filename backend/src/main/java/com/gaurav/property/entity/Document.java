package com.gaurav.property.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gaurav.property.enums.DocumentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private RegistrationApplication application;

    @Column(nullable = false, length = 50)
    private String documentType;

    @Column(nullable = false, length = 255)
    private String fileName;

    @JsonIgnore
    @Column(nullable = false, length = 500)
    private String storedPath;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;

    public Document(Long id, RegistrationApplication application, String documentType, String fileName, String storedPath, LocalDateTime uploadDate, DocumentStatus status) {
        this.id = id;
        this.application = application;
        this.documentType = documentType;
        this.fileName = fileName;
        this.storedPath = storedPath;
        this.uploadDate = uploadDate;
        this.status = status;
    }

    public Document() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistrationApplication getApplication() {
        return this.application;
    }

    public void setApplication(RegistrationApplication application) {
        this.application = application;
    }

    public String getDocumentType() {
        return this.documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoredPath() {
        return this.storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public LocalDateTime getUploadDate() {
        return this.uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public DocumentStatus getStatus() {
        return this.status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

}