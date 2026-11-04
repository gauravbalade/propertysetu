package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ApplicationAssignmentRequest {

    @NotNull
    private Long officerId;

    @NotBlank
    @Size(max = 500)
    private String reason;

    public Long getOfficerId() { return officerId; }
    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
