package com.paymentsbe.payment.api.dto;

import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentStatus;

public record PaymentApproveResponse(
        Long paymentId,
        String orderId,
        Long amount,
        PaymentStatus status
) {
    public static PaymentApproveResponse from(Payment payment) {
        return new PaymentApproveResponse(
                payment.getId(),
                payment.getOrder().getExternalId(),
                payment.getAmountKrw(),
                payment.getStatus()
        );
    }
}
