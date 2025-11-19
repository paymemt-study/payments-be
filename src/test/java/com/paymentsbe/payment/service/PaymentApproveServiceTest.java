package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderStatus;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.api.dto.PaymentApproveRequest;
import com.paymentsbe.payment.api.dto.PaymentApproveResponse;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentProvider;
import com.paymentsbe.payment.domain.PaymentStatus;
import com.paymentsbe.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApproveServiceTest {

    @Mock
    TossClient tossClient;

    @Mock
    OrderRepository orderRepository;

    @Mock
    PaymentRepository paymentRepository;

    @InjectMocks
    PaymentApproveService paymentApproveService;

    @Test
    @DisplayName("정상 결제 승인 시 Payment 저장 + Order 상태가 PAID 로 변경된다")
    void approve_success() {
        // given
        String orderExternalId = "ORD-12345678";
        Long amount = 50_000L;

        PaymentApproveRequest req = new PaymentApproveRequest(
                "payKey-1234",
                orderExternalId,
                amount
        );

        Order order = Order.builder()
                .id(1L)
                .externalId(orderExternalId)
                .totalAmountKrw(amount)
                .currency("KRW")
                .status(OrderStatus.PENDING)
                .build();

        given(orderRepository.findByExternalId(orderExternalId))
                .willReturn(Optional.of(order));

        Map<String, Object> tossResponse = Map.of(
                "paymentKey", "payKey-1234",
                "orderId", orderExternalId,
                "totalAmount", amount,
                "status", "DONE",
                "method", "카드"
        );

        given(tossClient.confirmPayment(req.paymentKey(), req.orderId(), req.amount()))
                .willReturn(tossResponse);

        // Payment.fromTossResponse 가 진짜 구현되어 있다고 가정하고 그대로 사용
        // paymentRepository.save() 호출 여부만 검증
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        // when
        PaymentApproveResponse resp = paymentApproveService.approve(req);

        // then
        // paymentRepository.save가 호출되었는지 + 저장되는 Payment 값 검증
        then(paymentRepository).should().save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();

        assertThat(saved.getOrder()).isEqualTo(order);
        assertThat(saved.getProvider()).isEqualTo(PaymentProvider.TOSS);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(saved.getAmountKrw()).isEqualTo(amount);

        // Order 상태 변경 확인
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        // 응답 DTO 최소 검증
        assertThat(resp.orderId()).isEqualTo(orderExternalId);
        assertThat(resp.amount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("주문이 없으면 ORDER_NOT_FOUND 예외를 던진다")
    void approve_order_not_found() {
        // given
        PaymentApproveRequest req = new PaymentApproveRequest(
                "payKey-1234",
                "ORD-NOT-EXISTS",
                10_000L
        );

        given(orderRepository.findByExternalId(req.orderId()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentApproveService.approve(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_NOT_FOUND);

        // Toss API는 아예 호출되지 않아야 함
        then(tossClient).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("금액이 일치하지 않으면 AMOUNT_MISMATCH 예외를 던진다")
    void approve_amount_mismatch() {
        // given
        String orderExternalId = "ORD-12345678";

        PaymentApproveRequest req = new PaymentApproveRequest(
                "payKey-1234",
                orderExternalId,
                50_000L
        );

        Order order = Order.builder()
                .id(1L)
                .externalId(orderExternalId)
                .totalAmountKrw(40_000L) // DB 주문 금액
                .currency("KRW")
                .status(OrderStatus.PENDING)
                .build();

        given(orderRepository.findByExternalId(orderExternalId))
                .willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentApproveService.approve(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AMOUNT_MISMATCH);

        // Toss 호출/Payment 저장 안 되어야 함
        then(tossClient).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 처리된 주문이면 ALREADY_PROCESSED 예외를 던진다")
    void approve_already_processed() {
        // given
        String orderExternalId = "ORD-12345678";

        PaymentApproveRequest req = new PaymentApproveRequest(
                "payKey-1234",
                orderExternalId,
                50_000L
        );

        Order order = Order.builder()
                .id(1L)
                .externalId(orderExternalId)
                .totalAmountKrw(50_000L)
                .currency("KRW")
                .status(OrderStatus.PAID) // 이미 결제 완료 상태
                .build();

        given(orderRepository.findByExternalId(orderExternalId))
                .willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentApproveService.approve(req))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_PROCESSED);

        then(tossClient).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }
}