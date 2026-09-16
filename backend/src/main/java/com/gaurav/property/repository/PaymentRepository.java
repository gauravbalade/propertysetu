package com.gaurav.property.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gaurav.property.entity.Payment;
import com.gaurav.property.enums.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    List<Payment> findByApplicationId(Long applicationId);

    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);
}