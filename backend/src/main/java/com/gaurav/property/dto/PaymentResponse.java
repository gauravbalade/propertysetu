package com.gaurav.property.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gaurav.property.enums.PaymentStatus;

public record PaymentResponse(
        Long id,
        ApplicationResponse application,
        String gatewayOrderId,
        String gatewayPaymentId,
        BigDecimal amount,
        LocalDateTime paymentDate,
        PaymentStatus paymentStatus,
        Boolean signatureVerified,
        String gatewayReference) {
}
