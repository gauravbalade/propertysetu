package com.gaurav.property.entity;

import java.time.LocalDateTime;

import com.gaurav.property.enums.VerificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private RegistrationApplication application;

    @Column(nullable = false)
    private LocalDateTime verificationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    @Column(length = 1000)
    private String remarks;

    @ManyToOne
    @JoinColumn(name = "verified_by")
    private UserAccount verifiedBy;

    public Verification(Long id, RegistrationApplication application, LocalDateTime verificationDate, VerificationStatus status, String remarks, UserAccount verifiedBy) {
        this.id = id;
        this.application = application;
        this.verificationDate = verificationDate;
        this.status = status;
        this.remarks = remarks;
        this.verifiedBy = verifiedBy;
    }

    public Verification() {
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

    public LocalDateTime getVerificationDate() {
        return this.verificationDate;
    }

    public void setVerificationDate(LocalDateTime verificationDate) {
        this.verificationDate = verificationDate;
    }

    public VerificationStatus getStatus() {
        return this.status;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public UserAccount getVerifiedBy() {
        return this.verifiedBy;
    }

    public void setVerifiedBy(UserAccount verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

}