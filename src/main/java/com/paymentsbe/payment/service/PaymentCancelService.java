package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.api.dto.PaymentCancelRequest;
import com.paymentsbe.payment.api.dto.PaymentCancelResponse;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final TossClient tossClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 카드 결제 취소(전액/부분 환불)
     */
    @Transactional
    public PaymentCancelResponse cancel(PaymentCancelRequest req) {
        // 1) 주문 조회
        Order order = orderRepository.findByExternalId(req.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2) 주문 상태 검증 (PAID가 아닌 주문은 취소 불가)
        if (!order.isPaid()) {
            throw new BusinessException(ErrorCode.CANNOT_CANCEL_ORDER);
        }

        // 3) 최신 결제 조회
        Payment payment = paymentRepository.findTopByOrderOrderByIdDesc(order)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 4) 결제 상태 검증 (이미 취소/환불된 건 제외)
        if (!payment.isPaid()) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        // 5) 취소 금액 결정 (요청이 없으면 전액 취소)
        Long cancelAmount = (req.amount() != null) ? req.amount() : payment.getAmountKrw();

        // 6) Toss 취소 API 호출
        Map<String, Object> tossResponse =
                tossClient.cancelPayment(payment.getProviderPaymentKey(), cancelAmount, req.reason());

        // 7) Toss 응답에서 실제 취소 금액 읽기 (없으면 요청값 사용)
        Long canceledAmount = cancelAmount;
        Object cancelAmountObj = tossResponse.get("cancelAmount");
        if (cancelAmountObj instanceof Number n) {
            canceledAmount = n.longValue();
        }

        // 8) Payment/Order 상태 전이
        if (canceledAmount.equals(payment.getAmountKrw())) {
            // 전액 환불
            payment.markRefunded();
            order.cancel();   // OrderStatus.CANCELED (이미 isPaid()였으므로 정상 취소)
        } else {
            // 부분 환불
            payment.markPartialRefund();
            // 주문은 여전히 PAID 상태 유지 (남은 금액 존재)
        }

        // 9) 응답 DTO
        return new PaymentCancelResponse(
                payment.getId(),
                order.getExternalId(),
                canceledAmount,
                payment.getStatus()
        );
    }
}