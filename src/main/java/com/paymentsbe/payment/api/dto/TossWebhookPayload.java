package com.paymentsbe.payment.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TossWebhookPayload(
        @JsonProperty("eventType") String eventType,   // ex) PAYMENT_STATUS_CHANGED
        @JsonProperty("createdAt") String createdAt,   // "2022-01-01T00:00:00.000000"
        @JsonProperty("data") Data data                // 실제 결제 정보
) {
    public record Data(
            @JsonProperty("mId") String mId,
            @JsonProperty("version") String version,
            @JsonProperty("lastTransactionKey") String lastTransactionKey,
            @JsonProperty("paymentKey") String paymentKey,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("status") String status,               // DONE, CANCELED, PARTIAL_CANCELED, FAILED ...
            @JsonProperty("requestedAt") String requestedAt,
            @JsonProperty("approvedAt") String approvedAt,
            @JsonProperty("canceledAt") String canceledAt,
            @JsonProperty("cancelAmount") Long cancelAmount      // 취소 금액 (없을 수도 있음)
            // card, virtualAccount 등은 필요 시 추가
    ) {}
}