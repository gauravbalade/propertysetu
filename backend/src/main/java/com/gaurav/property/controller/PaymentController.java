package com.gaurav.property.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gaurav.property.dto.PaymentOrderRequest;
import com.gaurav.property.dto.PaymentVerificationRequest;
import com.gaurav.property.entity.Payment;
import com.gaurav.property.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/order")
    public ResponseEntity<Payment> createOrder(
            @Valid @RequestBody PaymentOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Payment> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {

        return ResponseEntity.ok(
                paymentService.verifyPayment(request));
    }
}