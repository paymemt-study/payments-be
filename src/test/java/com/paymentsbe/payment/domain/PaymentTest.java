package com.paymentsbe.payment.domain;

import com.paymentsbe.order.domain.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    @DisplayName("fromTossResponse - status=DONE 이면 PAID 로 매핑된다")
    void fromTossResponse_paid() {
        // given
        Order order = Order.create(null, 50_000L);

        String approvedAtStr = "2025-11-16T16:39:11+09:00";

        Map<String, Object> tossResponse = new HashMap<>();
        tossResponse.put("paymentKey", "pay_1234");
        tossResponse.put("method", "CARD");
        tossResponse.put("totalAmount", 50_000);    // Number 타입이면 아무거나 가능 (Integer/Long)
        tossResponse.put("status", "DONE");
        tossResponse.put("approvedAt", approvedAtStr);

        // when
        Payment payment = Payment.fromTossResponse(order, tossResponse);

        // then
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getProvider()).isEqualTo(PaymentProvider.TOSS);
        assertThat(payment.getProviderPaymentKey()).isEqualTo("pay_1234");
        assertThat(payment.getMethod()).isEqualTo("CARD");
        assertThat(payment.getAmountKrw()).isEqualTo(50_000L);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);

        assertThat(payment.getApprovedAt())
                .isEqualTo(OffsetDateTime.parse(approvedAtStr));

        // rawPayload 에 JSON 이 잘 들어갔는지만 대략 체크
        assertThat(payment.getRawPayload())
                .isNotBlank()
                .contains("pay_1234");
    }

    @Test
    @DisplayName("fromTossResponse - status != DONE 이면 FAILED 로 매핑되고 approvedAt 은 null")
    void fromTossResponse_failed() {
        // given
        Order order = Order.create(null, 30_000L);

        Map<String, Object> tossResponse = new HashMap<>();
        tossResponse.put("paymentKey", "pay_fail");
        tossResponse.put("method", "CARD");
        tossResponse.put("totalAmount", 30_000);
        tossResponse.put("status", "CANCELED");
        // approvedAt 은 아예 없거나 null 이라고 가정

        // when
        Payment payment = Payment.fromTossResponse(order, tossResponse);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getApprovedAt()).isNull();
    }

    @Test
    @DisplayName("isPaid - 상태가 PAID 일 때만 true")
    void isPaid_works() {
        Payment paid = Payment.builder()
                .status(PaymentStatus.PAID)
                .build();

        Payment notPaid = Payment.builder()
                .status(PaymentStatus.FAILED)
                .build();

        assertThat(paid.isPaid()).isTrue();
        assertThat(notPaid.isPaid()).isFalse();
    }

    @Test
    @DisplayName("isCanceled - CANCELED, REFUNDED 는 true, 그 외는 false")
    void isCanceled_works() {
        Payment canceled = Payment.builder()
                .status(PaymentStatus.CANCELED)
                .build();

        Payment refunded = Payment.builder()
                .status(PaymentStatus.REFUNDED)
                .build();

        Payment paid = Payment.builder()
                .status(PaymentStatus.PAID)
                .build();

        assertThat(canceled.isCanceled()).isTrue();
        assertThat(refunded.isCanceled()).isTrue();
        assertThat(paid.isCanceled()).isFalse();
    }
}