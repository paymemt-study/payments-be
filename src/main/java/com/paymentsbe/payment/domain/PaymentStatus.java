package com.paymentsbe.payment.domain;

public enum PaymentStatus {
    INIT,
    PAID,
    PARTIAL_REFUND,
    REFUNDED,
    CANCELED,
    FAILED
}
