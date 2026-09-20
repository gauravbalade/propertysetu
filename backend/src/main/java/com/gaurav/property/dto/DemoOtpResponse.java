package com.gaurav.property.dto;

import lombok.Getter;

@Getter
public class DemoOtpResponse {

    private final String channel;
    private final String code;
    private final int expiresInSeconds;

    public DemoOtpResponse(String channel, String code, int expiresInSeconds) {
        this.channel = channel;
        this.code = code;
        this.expiresInSeconds = expiresInSeconds;
    }
}
