package com.gaurav.property.dto;

import com.gaurav.property.enums.UserRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminUserUpdateRequest {
    private UserRole role;
    private Boolean active;
    private Boolean emailVerified;
    private Boolean phoneVerified;
}
