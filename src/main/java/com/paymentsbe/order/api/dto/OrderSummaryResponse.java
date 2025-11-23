package com.paymentsbe.order.api.dto;

import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record OrderSummaryResponse(
        String orderId,
        Long amount,
        String currency,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderSummaryResponse from(Order order) {
        return OrderSummaryResponse.builder()
                .orderId(order.getExternalId())
                .amount(order.getTotalAmountKrw())
                .currency(order.getCurrency())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}