package com.paymentsbe.payment.domain;

import com.paymentsbe.common.entity.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_webhook_event")
@Getter
@SuperBuilder
@NoArgsConstructor
public class PaymentWebhookEvent extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(name = "payload", columnDefinition = "json")
    private String payload;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;
}