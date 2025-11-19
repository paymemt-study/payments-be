package com.paymentsbe.order.api.dto;

public record OrderCreateResponse(
        String orderId,
        Long amount
) {}
