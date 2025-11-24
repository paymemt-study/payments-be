package com.paymentsbe.payment.service;

import com.paymentsbe.common.util.JsonUtils;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.payment.api.dto.TossWebhookPayload;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentWebhookEvent;
import com.paymentsbe.payment.domain.Refund;
import com.paymentsbe.payment.repository.PaymentRepository;
import com.paymentsbe.payment.repository.PaymentWebhookEventRepository;
import com.paymentsbe.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentWebhookEventRepository eventRepository;

    @Transactional
    public void handleWebhook(TossWebhookPayload payload) {

        // 1) 멱등성 키: lastTransactionKey 사용
        String eventId = payload.data().lastTransactionKey();
        if (eventId == null || eventId.isBlank()) {
            // 혹시 몰라서 fallback (eventType + paymentKey + createdAt 조합)
            eventId = payload.eventType() + ":" + payload.data().paymentKey() + ":" + payload.createdAt();
        }

        // 이미 처리된 이벤트면 무시
        if (eventRepository.existsByEventId(eventId)) {
            log.info("[Webhook] Duplicate event ignored: {}", eventId);
            return;
        }

        // 2) Payment 조회 (paymentKey는 data 안에 있음)
        String paymentKey = payload.data().paymentKey();
        Payment payment = paymentRepository
                .findByProviderPaymentKey(paymentKey)
                .orElse(null); // 못 찾으면 null

        // 3) Webhook 이벤트 로그는 일단 먼저 남김 (payment null이어도)
        PaymentWebhookEvent event = PaymentWebhookEvent.builder()
                .eventId(eventId)
                .eventType(payload.eventType())          // ex) PAYMENT_STATUS_CHANGED
                .payment(payment)                        // null 가능
                .payload(JsonUtils.toJson(payload))
                .receivedAt(OffsetDateTime.now())
                .build();

        eventRepository.save(event);

        // payment 자체가 없으면 더 이상 동기화할 주문이 없으니 종료
        if (payment == null) {
            log.warn("[Webhook] Payment not found for paymentKey={}", paymentKey);
            return;
        }

        Order order = payment.getOrder();

        // 4) 상태 분기: 이제는 eventType이 아니라 data.status 로 분기해야 함
        String status = payload.data().status();   // DONE / CANCELED / PARTIAL_CANCELED / FAILED ...

        switch (status) {
            case "DONE" -> handleApproved(payment, order, payload);

            case "CANCELED", "PARTIAL_CANCELED" ->
                    handleCanceled(payment, order, payload, status);

            case "FAILED" -> handleFailed(payment, order);

            default -> log.warn("[Webhook] Unknown payment status={}, eventType={}",
                    status, payload.eventType());
        }
    }

    private void handleApproved(Payment payment, Order order, TossWebhookPayload payload) {
        payment.markPaid();
        order.markPaid();
        log.info("[Webhook] Payment approved synced. orderId={}", order.getExternalId());
    }

    private void handleFailed(Payment payment, Order order) {
        payment.markFailed();
        order.markFailed();
        log.info("[Webhook] Payment failed synced. orderId={}", order.getExternalId());
    }

    private void handleCanceled(Payment payment, Order order, TossWebhookPayload payload, String pgStatus) {
        Long cancelAmount = payload.data().cancelAmount();
        if (cancelAmount == null) {
            cancelAmount = payment.getRemainingAmountKrw();
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .refundAmountKrw(cancelAmount)
                .reason("PG_WEBHOOK")
                .refundedAt(OffsetDateTime.now())
                .rawPayload(JsonUtils.toJson(payload))
                .build();
        refundRepository.save(refund);

        payment.addRefundedAmount(cancelAmount);

        long remaining = payment.getRemainingAmountKrw();

        boolean partialByAmount = remaining > 0;
        boolean partialByPg = "PARTIAL_CANCELED".equals(pgStatus);

        if (partialByAmount || partialByPg) {
            payment.markPartialRefund();
            log.info("[Webhook] Partial refund synced. orderId={} cancelAmount={} remaining={}",
                    order.getExternalId(), cancelAmount, remaining);
        } else {
            payment.markRefunded();
            order.cancel();
            log.info("[Webhook] Full refund synced. orderId={} cancelAmount={}",
                    order.getExternalId(), cancelAmount);
        }
    }
}
