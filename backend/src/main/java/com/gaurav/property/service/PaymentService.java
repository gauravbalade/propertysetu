package com.gaurav.property.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gaurav.property.dto.ApplicationResponse;
import com.gaurav.property.dto.PaymentOrderRequest;
import com.gaurav.property.dto.PaymentResponse;
import com.gaurav.property.dto.PaymentVerificationRequest;
import com.gaurav.property.entity.Payment;
import com.gaurav.property.entity.RegistrationApplication;
import com.gaurav.property.enums.ApplicationStatus;
import com.gaurav.property.enums.PaymentStatus;
import com.gaurav.property.repository.PaymentRepository;
import com.gaurav.property.repository.RegistrationApplicationRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.razorpay.Order;

@Service
public class PaymentService {

    private static final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(500);

    private final PaymentRepository paymentRepository;
    private final RegistrationApplicationRepository applicationRepository;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    @Value("${RAZORPAY_KEY_ID:}")
    private String razorpayKeyId;

    @Value("${RAZORPAY_KEY_SECRET:}")
    private String razorpayKeySecret;

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
    public PaymentResponse createPaymentOrder(PaymentOrderRequest request) {
        RegistrationApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        if (application.getStatus() == ApplicationStatus.PAID
                || application.getStatus() == ApplicationStatus.UNDER_VERIFICATION
                || application.getStatus() == ApplicationStatus.VERIFIED
                || application.getStatus() == ApplicationStatus.COMPLETED) {
            return paymentRepository.findByApplicationId(application.getId()).stream()
                    .filter(existing -> existing.getPaymentStatus() == PaymentStatus.SUCCESS)
                    .findFirst()
                    .map(this::toResponse)
                    .orElseThrow(() -> new RuntimeException("Payment is already completed"));
        }

        if (application.getStatus() != ApplicationStatus.SUBMITTED
                && application.getStatus() != ApplicationStatus.PAYMENT_PENDING) {
            throw new RuntimeException("Payment is available only for submitted applications");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(PAYMENT_AMOUNT) != 0) {
            throw new RuntimeException("The payment amount is fixed at ₹500.00 for this academic demonstration.");
        }

        requireRazorpayConfiguration();

        if (application.getStatus() == ApplicationStatus.PAYMENT_PENDING) {
            Payment pending = paymentRepository.findByApplicationId(application.getId()).stream()
                    .filter(existing -> existing.getPaymentStatus() == PaymentStatus.PENDING)
                    .filter(existing -> "RAZORPAY".equalsIgnoreCase(existing.getGatewayReference()))
                    .findFirst()
                    .orElse(null);
            if (pending != null) {
                return toResponse(pending);
            }
        }

        try {
            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", 50000);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", application.getApplicationNumber());

            JSONObject notes = new JSONObject();
            notes.put("application_id", String.valueOf(application.getId()));
            notes.put("application_number", application.getApplicationNumber());
            orderRequest.put("notes", notes);

            Order order = client.orders.create(orderRequest);
            String orderId = order.get("id");

            Payment payment = new Payment();
            payment.setApplication(application);
            payment.setGatewayOrderId(orderId);
            payment.setAmount(PAYMENT_AMOUNT);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setSignatureVerified(false);
            payment.setGatewayReference("RAZORPAY");

            application.setStatus(ApplicationStatus.PAYMENT_PENDING);
            applicationRepository.save(application);
            Payment saved = paymentRepository.save(payment);

            auditService.record("RAZORPAY_ORDER_CREATED", "APPLICATION", application.getId(),
                    authorizationService.currentUser(), "Razorpay test order " + orderId);

            return toResponse(saved);
        } catch (RazorpayException ex) {
            throw new RuntimeException("Razorpay could not create the payment order. Please try again.");
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse getLatestPayment(Long applicationId) {
        RegistrationApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        authorizationService.requireOwner(application.getUserAccount());

        return paymentRepository.findByApplicationId(applicationId).stream()
                .max((left, right) -> {
                    LocalDateTime leftDate = left.getPaymentDate();
                    LocalDateTime rightDate = right.getPaymentDate();
                    if (leftDate == null && rightDate == null) return 0;
                    if (leftDate == null) return -1;
                    if (rightDate == null) return 1;
                    return leftDate.compareTo(rightDate);
                })
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public PaymentResponse verifyPayment(PaymentVerificationRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        RegistrationApplication application = payment.getApplication();
        authorizationService.requireOwner(application.getUserAccount());

        if (!"RAZORPAY".equalsIgnoreCase(payment.getGatewayReference())) {
            throw new RuntimeException("This payment record is not a Razorpay order.");
        }

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment has already been completed");
        }

        if (!payment.getGatewayOrderId().equals(request.getRazorpayOrderId())) {
            throw new RuntimeException("Payment order mismatch.");
        }

        requireRazorpayConfiguration();

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", payment.getGatewayOrderId());
            attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
            attributes.put("razorpay_signature", request.getRazorpaySignature());

            if (!Utils.verifyPaymentSignature(attributes, razorpayKeySecret)) {
                throw new RuntimeException("Razorpay payment signature could not be verified.");
            }

            RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            com.razorpay.Payment razorpayPayment = client.payments.fetch(request.getRazorpayPaymentId());
            String status = String.valueOf(razorpayPayment.get("status"));
            if (!"captured".equalsIgnoreCase(status)) {
                throw new RuntimeException("Razorpay payment is not captured yet.");
            }

            Object amountValue = razorpayPayment.get("amount");
            long capturedAmount = Long.parseLong(String.valueOf(amountValue));
            if (capturedAmount != 50000L) {
                throw new RuntimeException("The captured payment amount does not match the application fee.");
            }

            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setSignatureVerified(true);
            payment.setGatewayPaymentId(request.getRazorpayPaymentId());
            payment.setPaymentDate(LocalDateTime.now());
            application.setStatus(ApplicationStatus.PAID);

            applicationRepository.save(application);
            Payment saved = paymentRepository.save(payment);

            auditService.record("RAZORPAY_PAYMENT_VERIFIED", "PAYMENT", payment.getId(),
                    authorizationService.currentUser(), "Razorpay signature and captured amount verified");
            auditService.record("PAYMENT_COMPLETED", "APPLICATION", application.getId(),
                    authorizationService.currentUser(), "Razorpay test payment completed");

            return toResponse(saved);
        } catch (RazorpayException ex) {
            throw new RuntimeException("Unable to confirm the Razorpay payment. Please try again.");
        }
    }

    private void requireRazorpayConfiguration() {
        if (razorpayKeyId == null || razorpayKeyId.isBlank()
                || razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new RuntimeException(
                    "Razorpay test credentials are not configured on the server yet.");
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        RegistrationApplication application = payment.getApplication();
        ApplicationResponse applicationResponse = new ApplicationResponse(
                application.getId(),
                application.getApplicationNumber(),
                application.getUserAccount().getId(),
                application.getUserAccount().getUsername(),
                application.getUserAccount().getEmail(),
                application.getProperty().getId(),
                application.getProperty().getPropertyNumber(),
                application.getPurpose(),
                application.getApplicationDate(),
                application.getStatus(),
                application.getCreatedAt());

        return new PaymentResponse(
                payment.getId(),
                applicationResponse,
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getPaymentStatus(),
                payment.getSignatureVerified(),
                payment.getGatewayReference(),
                razorpayKeyId,
                "INR");
    }
}
