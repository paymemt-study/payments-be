package com.paymentsbe.order.api.dto;


import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderStatus;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record OrderDetailResponse(
        String orderId,
        Long amount,
        String currency,
        OrderStatus status,
        Instant createdAt,
        PaymentInfo payment
) {

    @Builder
    public record PaymentInfo(
            Long paymentId,
            String provider,
            String method,
            Long amountKrw,
            PaymentStatus status,
            Instant approvedAt
    ) {
        public static PaymentInfo from(Payment payment) {
            if (payment == null) return null;

            return PaymentInfo.builder()
                    .paymentId(payment.getId())
                    .provider(payment.getProvider().name())
                    .method(payment.getMethod())
                    .amountKrw(payment.getAmountKrw())
                    .status(payment.getStatus())
                    .approvedAt(payment.getApprovedAt() != null
                            ? payment.getApprovedAt().toInstant()
                            : null)
                    .build();
        }
    }

    public static OrderDetailResponse from(Order order, Payment payment) {
        return OrderDetailResponse.builder()
                .orderId(order.getExternalId())
                .amount(order.getTotalAmountKrw())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .payment(PaymentInfo.from(payment))
                .build();
    }
}