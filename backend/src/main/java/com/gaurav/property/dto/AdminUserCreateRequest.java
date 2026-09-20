package com.gaurav.property.dto;

import com.gaurav.property.enums.UserRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserCreateRequest {
    private String username;
    private String email;
    private String phone;
    private String password;
    private UserRole role = UserRole.OFFICER;
}
