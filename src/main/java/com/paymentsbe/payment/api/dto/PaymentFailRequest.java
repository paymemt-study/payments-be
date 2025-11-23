package com.paymentsbe.payment.api.dto;


public record PaymentFailRequest(
        String orderId,   // ORD-xxxx
        String code,      // 토스 에러 코드 (ex: PAY_PROCESS_CANCELED)
        String message    // 토스 에러 메시지
) {
}
