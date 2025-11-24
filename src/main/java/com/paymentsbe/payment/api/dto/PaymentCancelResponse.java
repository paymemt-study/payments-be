package com.paymentsbe.payment.api.dto;

import com.paymentsbe.payment.domain.PaymentStatus;

public record PaymentCancelResponse(
        Long paymentId,
        String orderId,
        Long canceledAmount,
        PaymentStatus status
) {
}
