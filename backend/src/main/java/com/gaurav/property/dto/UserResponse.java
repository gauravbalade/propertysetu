package com.gaurav.property.dto;

import com.gaurav.property.enums.UserRole;

import lombok.Getter;

@Getter
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private String token;
    private boolean verificationRequired;
    private boolean emailVerified;
    private boolean phoneVerified;

    public UserResponse(Long id, String username, String email, String phone, UserRole role) {
        this(id, username, email, phone, role, null, false, false, false);
    }

    public UserResponse(Long id, String username, String email, String phone,
            UserRole role, String token) {
        this(id, username, email, phone, role, token, false, false, false);
    }

    public UserResponse(Long id, String username, String email, String phone,
            UserRole role, String token, boolean verificationRequired,
            boolean emailVerified, boolean phoneVerified) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.token = token;
        this.verificationRequired = verificationRequired;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }
}
