package com.gaurav.property.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final String apiKey;
    private final String fromEmail;
    private final String fromName;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public NotificationService(
            @Value("${SENDGRID_API_KEY:}") String apiKey,
            @Value("${SENDGRID_FROM_EMAIL:}") String fromEmail,
            @Value("${SENDGRID_FROM_NAME:PropertySetu}") String fromName) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.fromEmail = fromEmail == null ? "" : fromEmail.trim();
        this.fromName = fromName == null || fromName.isBlank() ? "PropertySetu" : fromName.trim();
    }

    public boolean isConfigured() {
        return !apiKey.isBlank() && !fromEmail.isBlank();
    }

    /**
     * Transactional email is intentionally best-effort. A notification provider
     * outage must never roll back an application submission or successful payment.
     */
    public boolean sendApplicationStatus(
            String recipient,
            String applicationNumber,
            String propertyReference,
            String status,
            String nextStep) {

        if (!isConfigured() || recipient == null || recipient.isBlank()) {
            return false;
        }

        String subject = "PropertySetu application update · " + applicationNumber;
        String text = String.format(
                "PropertySetu academic demonstration%n%n"
                        + "Application: %s%n"
                        + "Property reference: %s%n"
                        + "Current status: %s%n%n"
                        + "%s%n%n"
                        + "This is an academic project notification. "
                        + "It is not an official government registration notice.",
                applicationNumber,
                propertyReference,
                status,
                nextStep);

        JSONObject payload = new JSONObject();
        payload.put("personalizations", new org.json.JSONArray()
                .put(new JSONObject().put("to", new org.json.JSONArray()
                        .put(new JSONObject().put("email", recipient)))));
        payload.put("from", new JSONObject()
                .put("email", fromEmail)
                .put("name", fromName));
        payload.put("subject", subject);
        payload.put("content", new org.json.JSONArray()
                .put(new JSONObject()
                        .put("type", "text/plain")
                        .put("value", text)));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.sendgrid.com/v3/mail/send"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            payload.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
