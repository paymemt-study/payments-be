package com.paymentsbe.order.domain;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    @DisplayName("주문 생성 시 기본 상태는 PENDING")
    void createOrder_initialStatusPending() {
        Order order = Order.create(null, 50000L);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(order.isPending());
    }

    @Test
    @DisplayName("markPaid() → PENDING 상태에서만 가능")
    void markPaid_success() {
        Order order = Order.create(null, 50000L);
        order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertTrue(order.isPaid());
    }

    @Test
    @DisplayName("markPaid() 실패 - 이미 PAID 상태면 예외 발생")
    void markPaid_alreadyProcessed() {
        Order order = Order.create(null, 50000L);
        order.markPaid();

        BusinessException ex = assertThrows(
                BusinessException.class,
                order::markPaid
        );

        assertEquals(ErrorCode.ALREADY_PROCESSED, ex.getErrorCode());
    }

    @Test
    @DisplayName("markFailed() → PENDING 상태일 때 FAILED로 변경")
    void markFailed_success() {
        Order order = Order.create(null, 50000L);
        order.markFailed();
        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertTrue(order.isFailed());
    }

    @Test
    @DisplayName("markFailed() → PAID 상태에서는 상태 변경 없음")
    void markFailed_noEffectIfPaid() {
        Order order = Order.create(null, 50000L);
        order.markPaid();
        order.markFailed(); // 무시됨
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("cancel() → PAID 상태에서만 가능")
    void cancel_success() {
        Order order = Order.create(null, 30000L);
        order.markPaid();
        order.cancel();

        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertTrue(order.isCanceled());
    }

    @Test
    @DisplayName("cancel() 실패 - PENDING 상태에서는 취소 불가")
    void cancel_failWhenPending() {
        Order order = Order.create(null, 30000L);

        BusinessException ex = assertThrows(
                BusinessException.class,
                order::cancel
        );

        assertEquals(ErrorCode.CANNOT_CANCEL_ORDER, ex.getErrorCode());
    }

    @Test
    @DisplayName("cancel() 실패 - FAILED 상태에서도 취소 불가")
    void cancel_failWhenFailed() {
        Order order = Order.create(null, 30000L);
        order.markFailed();

        BusinessException ex = assertThrows(
                BusinessException.class,
                order::cancel
        );

        assertEquals(ErrorCode.CANNOT_CANCEL_ORDER, ex.getErrorCode());
    }
}