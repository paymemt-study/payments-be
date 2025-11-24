package com.paymentsbe.order.api.dto;

import java.util.List;

public record CreateOrderRequest(
        Long userId,                  // 로그인 붙기 전까지는 하드코딩 or 프론트에서 전달
        List<OrderItemRequest> items  // 구매할 상품 목록
) {
    public record OrderItemRequest(
            Long productId,
            Integer quantity
    ) {}
}
