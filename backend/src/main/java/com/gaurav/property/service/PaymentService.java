package com.gaurav.property.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.PaymentOrderRequest;
import com.gaurav.property.dto.PaymentVerificationRequest;
import com.gaurav.property.entity.Payment;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.enums.PaymentStatus;
import com.gaurav.property.repository.PaymentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RegistrationApplicationRepository applicationRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    public PaymentService(PaymentRepository paymentRepository,
            RegistrationApplicationRepository applicationRepository,
            AuthorizationService authorizationService,
            AuditService auditService) {
        this.paymentRepository = paymentRepository;
        this.applicationRepository = applicationRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional
    public Payment createPaymentOrder(PaymentOrderRequest request) {
        RegistrationApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        if (application.getStatus() != ApplicationStatus.SUBMITTED
                && application.getStatus() != ApplicationStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Payment is available only for submitted applications");
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        Payment payment = new Payment();
        payment.setApplication(application);
        payment.setGatewayOrderId("TEST_ORDER_" + UUID.randomUUID().toString().substring(0, 10).toUpperCase());
        payment.setAmount(request.getAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setSignatureVerified(false);
        payment.setGatewayReference("TEST_MODE");

        application.setStatus(ApplicationStatus.PAYMENT_PENDING);
        applicationRepository.save(application);
        Payment saved = paymentRepository.save(payment);
        auditService.record("PAYMENT_ORDER_CREATED", "APPLICATION", application.getId(),
                authorizationService.currentUser(), "Test-mode order " + payment.getGatewayOrderId());
        return saved;
    }

    @Transactional
    public Payment verifyPayment(PaymentVerificationRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        RegistrationApplication application = payment.getApplication();
        authorizationService.requireOwner(application.getUserAccount());

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment has already been completed");
        }
        if (!Boolean.TRUE.equals(request.getSuccessful())) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setSignatureVerified(false);
            payment.setGatewayPaymentId(request.getPaymentReference());
            Payment saved = paymentRepository.save(payment);
            auditService.record("PAYMENT_FAILED", "PAYMENT", payment.getId(),
                    authorizationService.currentUser(), "Payment marked unsuccessful");
            return saved;
        }

        if (request.getPaymentReference() == null || request.getPaymentReference().isBlank()) {
            throw new RuntimeException("Payment reference is required");
        }
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setSignatureVerified(true);
        payment.setGatewayPaymentId(request.getPaymentReference());
        payment.setPaymentDate(LocalDateTime.now());
        application.setStatus(ApplicationStatus.PAID);
        applicationRepository.save(application);
        Payment saved = paymentRepository.save(payment);
        auditService.record("PAYMENT_SUCCEEDED", "PAYMENT", payment.getId(),
                authorizationService.currentUser(), "Test-mode payment reference recorded");
        return saved;
    }
}
