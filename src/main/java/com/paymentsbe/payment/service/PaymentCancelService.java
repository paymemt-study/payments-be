package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.common.util.JsonUtils;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.api.dto.PaymentCancelRequest;
import com.paymentsbe.payment.api.dto.PaymentCancelResponse;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentStatus;
import com.paymentsbe.payment.domain.Refund;
import com.paymentsbe.payment.repository.PaymentRepository;
import com.paymentsbe.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private final TossClient tossClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

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

        // 4) 결제 상태 검증
        //    - PAID 또는 PARTIAL_REFUND 상태에서만 추가 환불 허용
        if (!(payment.isPaid() || payment.getStatus() == PaymentStatus.PARTIAL_REFUND)) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        Long remaining = payment.getRemainingAmountKrw();

        // 5) 취소 금액 결정 (요청이 없으면 남은 금액 전체 취소)
        Long requestedAmount = req.amount();
        Long cancelAmount = (requestedAmount != null) ? requestedAmount : remaining;

        if (cancelAmount == null || cancelAmount <= 0 || cancelAmount > remaining) {
            // 필요 시 ErrorCode에 INVALID_REFUND_AMOUNT 추가
            throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
        }

        // 6) Toss 취소 API 호출
        Map<String, Object> tossResponse =
                tossClient.cancelPayment(payment.getProviderPaymentKey(), cancelAmount, req.reason());

        // 7) Toss 응답에서 실제 취소 금액 읽기 (없으면 요청값 사용)
        Long canceledAmount = cancelAmount;
        Object cancelAmountObj = tossResponse.get("cancelAmount");
        if (cancelAmountObj instanceof Number n) {
            canceledAmount = n.longValue();
        }

        // 8) Refund 엔티티 저장
        Refund refund = Refund.builder()
                .payment(payment)
                .refundAmountKrw(canceledAmount)
                .reason(req.reason() != null ? req.reason() : "USER_REQUEST")
                .refundedAt(OffsetDateTime.now()) // 또는 tossResponse에서 cancelAt 파싱
                .rawPayload(JsonUtils.toJson(tossResponse))
                .build();
        refundRepository.save(refund);

        // 9) Payment 누적 환불 금액 갱신
        payment.addRefundedAmount(canceledAmount);

        // 10) Payment/Order 상태 전이
        Long afterRemaining = payment.getRemainingAmountKrw();
        if (afterRemaining == 0L) {
            // 전액 환불 완료
            payment.markRefunded();
            order.cancel();   // OrderStatus.CANCELED
        } else {
            // 부분 환불
            payment.markPartialRefund();
            // 주문은 여전히 PAID 상태 유지
        }

        // 11) 응답 DTO
        return new PaymentCancelResponse(
                payment.getId(),
                order.getExternalId(),
                canceledAmount,
                payment.getStatus()
        );
    }
}