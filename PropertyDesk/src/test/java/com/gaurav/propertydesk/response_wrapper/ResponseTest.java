package com.gaurav.propertydesk.response_wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ResponseTest {
    @Test
    void sendShouldReturnMessageDataAndStatus() {
        Response response = new Response();
        ResponseEntity<CustomResponse> result = response.send("Saved", "sample-data", HttpStatus.CREATED);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Saved", result.getBody().getMessage());
        assertEquals("sample-data", result.getBody().getData());
    }
}