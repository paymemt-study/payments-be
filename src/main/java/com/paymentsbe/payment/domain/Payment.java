package com.paymentsbe.payment.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import com.paymentsbe.common.util.JsonUtils;
import com.paymentsbe.order.domain.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "payment")
@Getter
@SuperBuilder
@NoArgsConstructor
public class Payment extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private PaymentProvider provider;

    @Column(name = "provider_payment_key", nullable = false, unique = true, length = 128)
    private String providerPaymentKey;

    @Column(name = "method", nullable = false, length = 32)
    private String method; // CARD, TRANSFER, ...

    @Column(name = "amount_krw", nullable = false)
    private Long amountKrw;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    // JSON 전체를 문자열로 저장 (MySQL json 컬럼에 매핑)
    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;

    //정적 팩토리: Toss 응답 → Payment
    @SuppressWarnings("unchecked")
    public static Payment fromTossResponse(Order order, Map<String, Object> tossResponse) {
        String paymentKey = (String) tossResponse.get("paymentKey");
        String method = (String) tossResponse.get("method");
        Number totalAmount = (Number) tossResponse.get("totalAmount");
        String status = (String) tossResponse.get("status");
        String approvedAtStr = (String) tossResponse.get("approvedAt"); // "2025-11-16T16:39:11+09:00"

        OffsetDateTime approvedAt = approvedAtStr != null
                ? OffsetDateTime.parse(approvedAtStr)
                : null;

        return Payment.builder()
                .order(order)
                .provider(PaymentProvider.TOSS)
                .providerPaymentKey(paymentKey)
                .method(method)
                .amountKrw(totalAmount.longValue())
                .status("DONE".equals(status) ? PaymentStatus.PAID : PaymentStatus.FAILED)
                .approvedAt(approvedAt)
                .rawPayload(JsonUtils.toJson(tossResponse))
                .build();
    }
    public boolean isPaid() {
        return this.status == PaymentStatus.PAID;
    }

    public boolean isCanceled() {
        return this.status == PaymentStatus.CANCELED || this.status == PaymentStatus.REFUNDED;
    }
}
