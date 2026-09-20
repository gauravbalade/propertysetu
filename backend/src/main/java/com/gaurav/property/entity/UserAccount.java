package com.gaurav.property.entity;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.gaurav.property.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
        private Boolean emailVerified = false;

    @Column(nullable = false)
        private Boolean phoneVerified = false;

    @Column(length = 50)
    private String emailVerificationSid;

    @Column(length = 50)
    private String phoneVerificationSid;

    @Column(length = 50)
    private String passwordResetVerificationSid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
        private UserRole role = UserRole.APPLICANT;

    @Column(nullable = false)
        private Boolean active = true;

    @Column(nullable = false)
        private LocalDateTime createdAt = LocalDateTime.now();

    public UserAccount(Long id, String username, String password, String email, String phone, String emailVerificationSid, String phoneVerificationSid, String passwordResetVerificationSid) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.emailVerificationSid = emailVerificationSid;
        this.phoneVerificationSid = phoneVerificationSid;
        this.passwordResetVerificationSid = passwordResetVerificationSid;
    }

    public UserAccount() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmailVerificationSid() {
        return this.emailVerificationSid;
    }

    public void setEmailVerificationSid(String emailVerificationSid) {
        this.emailVerificationSid = emailVerificationSid;
    }

    public String getPhoneVerificationSid() {
        return this.phoneVerificationSid;
    }

    public void setPhoneVerificationSid(String phoneVerificationSid) {
        this.phoneVerificationSid = phoneVerificationSid;
    }

    public String getPasswordResetVerificationSid() {
        return this.passwordResetVerificationSid;
    }

    public void setPasswordResetVerificationSid(String passwordResetVerificationSid) {
        this.passwordResetVerificationSid = passwordResetVerificationSid;
    }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }
    public Boolean getPhoneVerified() { return phoneVerified; }
    public void setPhoneVerified(Boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private final UserAccount value = new UserAccount();
        public Builder username(String value) { this.value.username = value; return this; }
        public Builder password(String value) { this.value.password = value; return this; }
        public Builder email(String value) { this.value.email = value; return this; }
        public Builder phone(String value) { this.value.phone = value; return this; }
        public Builder role(UserRole value) { this.value.role = value; return this; }
        public Builder active(Boolean value) { this.value.active = value; return this; }
        public Builder emailVerified(Boolean value) { this.value.emailVerified = value; return this; }
        public Builder phoneVerified(Boolean value) { this.value.phoneVerified = value; return this; }
        public Builder emailVerificationSid(String value) { this.value.emailVerificationSid = value; return this; }
        public Builder phoneVerificationSid(String value) { this.value.phoneVerificationSid = value; return this; }
        public Builder passwordResetVerificationSid(String value) { this.value.passwordResetVerificationSid = value; return this; }
        public Builder createdAt(LocalDateTime value) { this.value.createdAt = value; return this; }
        public UserAccount build() { return this.value; }
    }

}