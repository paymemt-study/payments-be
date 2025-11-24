package com.paymentsbe.order.api.dto;

public record CreateOrderResponse(
        String orderId,        // 외부 노출용 주문번호 (Order.externalId)
        Long totalAmountKrw,   // 결제 총액
        String currency
) {}
