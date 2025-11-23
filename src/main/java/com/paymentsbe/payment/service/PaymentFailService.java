package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.api.dto.PaymentFailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFailService {

    private final OrderRepository orderRepository;

    /**
     * 토스 failUrl 콜백에서 호출되는 실패 처리 API.
     * - 주문이 PENDING이면 FAILED로 전이
     * - 이미 처리된 주문(PAID/FAILED/CANCELED)은 무시 (멱등)
     */
    @Transactional
    public void handleFail(PaymentFailRequest req) {
        Order order = orderRepository.findByExternalId(req.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 이미 처리된 주문은 무시
        if (!order.isPending()) {
            return;
        }

        // 최초 실패 처리
        order.markFailed();
        // TODO: 필요해지면 code/message 를 별도 FailureLog 등에 저장 가능
    }
}