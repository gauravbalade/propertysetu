package com.gaurav.property.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.PaymentOrderRequest;
import com.gaurav.property.dto.PaymentResponse;
import com.gaurav.property.dto.PaymentVerificationRequest;
import com.gaurav.property.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<PaymentResponse> getLatestPayment(@PathVariable Long applicationId) {
        PaymentResponse payment = paymentService.getLatestPayment(applicationId);
        return payment == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(payment);
    }

    @PostMapping("/order")
    public ResponseEntity<PaymentResponse> createOrder(
            @Valid @RequestBody PaymentOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {

        return ResponseEntity.ok(
                paymentService.verifyPayment(request));
    }
}