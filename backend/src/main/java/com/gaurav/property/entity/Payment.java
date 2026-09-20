package com.gaurav.property.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.gaurav.property.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private RegistrationApplication application;

    @Column(nullable = false, unique = true, length = 100)
    private String gatewayOrderId;

    @Column(length = 100)
    private String gatewayPaymentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private Boolean signatureVerified;

    @Column(length = 255)
    private String gatewayReference;



    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegistrationApplication getApplication() {
        return this.application;
    }

    public void setApplication(RegistrationApplication application) {
        this.application = application;
    }

    public String getGatewayOrderId() {
        return this.gatewayOrderId;
    }

    public void setGatewayOrderId(String gatewayOrderId) {
        this.gatewayOrderId = gatewayOrderId;
    }

    public String getGatewayPaymentId() {
        return this.gatewayPaymentId;
    }

    public void setGatewayPaymentId(String gatewayPaymentId) {
        this.gatewayPaymentId = gatewayPaymentId;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return this.paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentStatus getPaymentStatus() {
        return this.paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Boolean getSignatureVerified() {
        return this.signatureVerified;
    }

    public void setSignatureVerified(Boolean signatureVerified) {
        this.signatureVerified = signatureVerified;
    }

    public String getGatewayReference() {
        return this.gatewayReference;
    }

    public void setGatewayReference(String gatewayReference) {
        this.gatewayReference = gatewayReference;
    }

}