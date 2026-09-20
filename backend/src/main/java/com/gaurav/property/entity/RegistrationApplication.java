package com.gaurav.property.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gaurav.property.enums.ApplicationStatus;

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
@Table(name = "registration_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String applicationNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private LocalDate applicationDate;

    @Column(nullable = false, length = 255)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;



    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return this.applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public UserAccount getUserAccount() {
        return this.userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public Property getProperty() {
        return this.property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public LocalDate getApplicationDate() {
        return this.applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public ApplicationStatus getStatus() {
        return this.status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}