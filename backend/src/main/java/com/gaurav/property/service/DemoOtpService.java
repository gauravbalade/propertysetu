package com.gaurav.property.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DemoOtpService {

    private static final long TTL_SECONDS = 10 * 60;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entry> activeOtps = new ConcurrentHashMap<>();
    private final boolean enabled;

    public DemoOtpService(@Value("${DEMO_OTP_ENABLED:false}") boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String issue(String username, String channel) {
        requireEnabled();
        String normalizedChannel = normalizeChannel(channel);
        String code = String.format("%06d", random.nextInt(1_000_000));
        activeOtps.put(key(username, normalizedChannel),
                new Entry(code, Instant.now().plusSeconds(TTL_SECONDS)));
        return code;
    }

    public boolean verify(String username, String channel, String code) {
        requireEnabled();
        String normalizedChannel = normalizeChannel(channel);
        Entry entry = activeOtps.get(key(username, normalizedChannel));
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            activeOtps.remove(key(username, normalizedChannel));
            return false;
        }

        boolean matches = entry.code().equals(code == null ? "" : code.trim());
        if (matches && !"RESET".equals(normalizedChannel)) {
            activeOtps.remove(key(username, normalizedChannel));
        }
        return matches;
    }

    public void clear(String username, String channel) {
        activeOtps.remove(key(username, normalizeChannel(channel)));
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new RuntimeException("Academic demo OTP fallback is disabled.");
        }
    }

    private String normalizeChannel(String channel) {
        String value = channel == null ? "" : channel.trim().toUpperCase();
        if (!value.equals("EMAIL") && !value.equals("PHONE") && !value.equals("RESET")) {
            throw new RuntimeException("OTP channel must be EMAIL, PHONE or RESET.");
        }
        return value;
    }

    private String key(String username, String channel) {
        return username.trim().toLowerCase() + ":" + channel;
    }

    private record Entry(String code, Instant expiresAt) {}
}
