package com.gaurav.property.dto;

import jakarta.validation.constraints.NotBlank;

public class Msg91VerificationRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String channel;

    @NotBlank
    private String accessToken;

    public Msg91VerificationRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
