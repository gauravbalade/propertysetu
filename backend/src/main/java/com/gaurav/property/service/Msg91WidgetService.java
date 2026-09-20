package com.gaurav.property.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class Msg91WidgetService {

    private static final String VERIFY_URL =
            "https://control.msg91.com/api/v5/widget/verifyAccessToken";

    private final String authKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public Msg91WidgetService(
            @Value("${MSG91_AUTHKEY:}") String authKey,
            ObjectMapper objectMapper) {
        this.authKey = authKey;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public JsonNode verifyAccessToken(String accessToken) {
        if (authKey == null || authKey.isBlank()) {
            throw new RuntimeException(
                    "MSG91 verification is not configured on the server.");
        }

        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("MSG91 verification token is missing.");
        }

        try {
            String body = objectMapper.createObjectNode()
                    .put("authkey", authKey)
                    .put("access-token", accessToken.trim())
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERIFY_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode json = parseResponse(response.body());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("MSG91 could not verify the OTP session.");
            }

            if (!isSuccessful(json)) {
                throw new RuntimeException("MSG91 could not verify the OTP session.");
            }

            return json;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("MSG91 verification was interrupted. Please try again.");
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException("Unable to verify the MSG91 OTP session. Please try again.");
        }
    }

    public boolean containsIdentifier(JsonNode node, String identifier) {
        String expected = normalize(identifier);
        if (expected.isBlank()) {
            return false;
        }
        return containsNormalizedValue(node, expected);
    }

    private boolean containsNormalizedValue(JsonNode node, String expected) {
        if (node == null) {
            return false;
        }

        if (node.isTextual() && normalize(node.asText()).equals(expected)) {
            return true;
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                if (containsNormalizedValue(child, expected)) {
                    return true;
                }
            }
        }

        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                if (containsNormalizedValue(fields.next().getValue(), expected)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isSuccessful(JsonNode node) {
        if (node == null || node.isMissingNode()) {
            return false;
        }

        if (node.isObject()) {
            JsonNode type = node.get("type");
            if (type != null && type.isTextual()) {
                String value = type.asText().toLowerCase();
                if (value.contains("success")) {
                    return true;
                }
                if (value.contains("error") || value.contains("failure")) {
                    return false;
                }
            }

            JsonNode status = node.get("status");
            if (status != null && status.isTextual()) {
                String value = status.asText().toLowerCase();
                if (value.equals("success") || value.equals("verified")
                        || value.equals("true") || value.equals("200")) {
                    return true;
                }
            }

            JsonNode message = node.get("message");
            if (message != null && message.isTextual()) {
                String value = message.asText().toLowerCase();
                if (value.contains("success") || value.contains("verified")) {
                    return true;
                }
            }

            JsonNode data = node.get("data");
            if (data != null && !data.isNull()) {
                JsonNode dataStatus = data.isObject() ? data.get("status") : null;
                if (dataStatus != null && dataStatus.isTextual()) {
                    String value = dataStatus.asText().toLowerCase();
                    return value.equals("success") || value.equals("verified")
                            || value.equals("true") || value.equals("200");
                }
            }
        }

        return false;
    }

    private JsonNode parseResponse(String body) {
        try {
            return objectMapper.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (Exception ex) {
            throw new RuntimeException("MSG91 returned an unreadable verification response.");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String text = value.trim().toLowerCase();
        String digits = text.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "91" + digits;
        }
        if (digits.length() == 12 && digits.startsWith("91")) {
            return digits;
        }
        return text;
    }
}
