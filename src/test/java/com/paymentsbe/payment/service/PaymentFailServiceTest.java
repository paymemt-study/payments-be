package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.domain.OrderStatus;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.api.dto.PaymentFailRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentFailServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentFailService paymentFailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("PENDING 주문에 대해 fail을 호출하면 FAILED 상태로 변경된다")
    void handleFail_pendingOrder_becomesFailed() {
        // given
        String externalId = "ORD-FAIL-0001";
        PaymentFailRequest req = new PaymentFailRequest(
                externalId,
                "PAY_PROCESS_CANCELED",
                "사용자가 결제를 취소했습니다."
        );

        Order pendingOrder = Order.builder()
                .id(1L)
                .externalId(externalId)
                .totalAmountKrw(50_000L)
                .currency("KRW")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByExternalId(externalId))
                .thenReturn(Optional.of(pendingOrder));

        // when
        paymentFailService.handleFail(req);

        // then
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
    }

    @Test
    @DisplayName("존재하지 않는 주문에 대해 fail을 호출하면 ORDER_NOT_FOUND 예외가 발생한다")
    void handleFail_orderNotFound() {
        // given
        String externalId = "ORD-NOPE-9999";
        PaymentFailRequest req = new PaymentFailRequest(
                externalId,
                "NOT_FOUND",
                "존재하지 않는 주문"
        );

        when(orderRepository.findByExternalId(externalId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentFailService.handleFail(req))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 PAID 상태의 주문에 대해 fail을 호출하면 상태는 유지된다")
    void handleFail_alreadyPaid_doesNothing() {
        // given
        String externalId = "ORD-PAID-1234";
        PaymentFailRequest req = new PaymentFailRequest(
                externalId,
                "PAY_PROCESS_CANCELED",
                "이미 결제 완료된 주문"
        );

        Order paidOrder = Order.builder()
                .id(1L)
                .externalId(externalId)
                .totalAmountKrw(50_000L)
                .currency("KRW")
                .status(OrderStatus.PAID)
                .build();

        when(orderRepository.findByExternalId(externalId))
                .thenReturn(Optional.of(paidOrder));

        // when
        paymentFailService.handleFail(req);

        // then
        assertThat(paidOrder.getStatus()).isEqualTo(OrderStatus.PAID);
    }
}