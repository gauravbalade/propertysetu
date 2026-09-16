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

    public UserResponse(Long id, String username, String email, String phone, UserRole role) {
        this(id, username, email, phone, role, null);
    }

    public UserResponse(Long id, String username, String email, String phone,
            UserRole role, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.token = token;
    }
}