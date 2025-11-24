package com.paymentsbe.payment.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "refund")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 하나의 Payment에 대해 여러 번 환불 가능
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "refund_amount_krw", nullable = false)
    private Long refundAmountKrw;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "refunded_at", nullable = false)
    private OffsetDateTime refundedAt;

    // Toss /payments/{paymentKey}/cancel 응답 JSON 전체
    @Column(name = "raw_payload", columnDefinition = "json")
    private String rawPayload;
}