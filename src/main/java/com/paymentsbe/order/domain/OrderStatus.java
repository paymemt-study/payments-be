package com.paymentsbe.order.domain;

public enum OrderStatus {
    PENDING,   // 결제 전
    PAID,      // 결제 완료
    CANCELED,  // 주문 취소
    FAILED     // 결제 실패
}
