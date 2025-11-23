package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.BusinessException;
import com.paymentsbe.common.exception.ErrorCode;
import com.paymentsbe.order.domain.Order;
import com.paymentsbe.order.repository.OrderRepository;
import com.paymentsbe.payment.api.dto.PaymentApproveRequest;
import com.paymentsbe.payment.api.dto.PaymentApproveResponse;
import com.paymentsbe.payment.domain.Payment;
import com.paymentsbe.payment.domain.PaymentStatus;
import com.paymentsbe.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentApproveService {

    private final TossClient tossClient;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentApproveResponse approve(PaymentApproveRequest req) {
        // 1) 주문 조회
        Order order = orderRepository.findByExternalId(req.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2) 금액 검증
        if (!order.getTotalAmountKrw().equals(req.amount())) {
            throw new BusinessException(ErrorCode.AMOUNT_MISMATCH);
        }

        if (!order.isPending()) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED);
        }

        // 3) Toss 승인 호출
        Map<String, Object> tossResponse;
        try {
            tossResponse = tossClient.confirmPayment(
                    req.paymentKey(),
                    req.orderId(),
                    req.amount()
            );
        } catch (RuntimeException e) {
            order.markFailed();
            throw e;
        }

        // 4) Payment 생성 & 저장
        Payment payment = Payment.fromTossResponse(order, tossResponse);
        paymentRepository.save(payment);

        // 5) Order 상태 변경
        if (payment.getStatus() == PaymentStatus.PAID) {
            order.markPaid();
        } else {
            order.markFailed();
        }

        // 6) 응답 DTO
        return PaymentApproveResponse.from(payment);
    }
}