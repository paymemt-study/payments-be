package com.paymentsbe.payment.api.dto;

public record PaymentApproveRequest(
        String paymentKey,
        String orderId,
        Long amount
) { }
