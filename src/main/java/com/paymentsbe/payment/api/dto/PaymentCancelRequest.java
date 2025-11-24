package com.paymentsbe.payment.api.dto;

public record PaymentCancelRequest(
        String orderId,   // ORD-xxxx (우리 쪽 주문 번호)
        Long amount,      // 취소 금액 (null이면 전액 취소로 간주)
        String reason     // 취소 사유 (고객 요청, 단순 변심 등)
) {
}
