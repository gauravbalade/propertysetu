package com.gaurav.propertydesk.response_wrapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class Response {
    public ResponseEntity<CustomResponse> send(String message, Object data, HttpStatus status) {
        return new ResponseEntity<>(new CustomResponse(message, data), status);
    }
}