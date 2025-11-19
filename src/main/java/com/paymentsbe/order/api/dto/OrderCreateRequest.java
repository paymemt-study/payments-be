package com.paymentsbe.order.api.dto;

public record OrderCreateRequest(
        Long userId,
        Long amount
) {}
