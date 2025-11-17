package com.paymentsbe.payment.api.controller;

import com.paymentsbe.payment.api.dto.PaymentApproveRequest;
import com.paymentsbe.payment.service.TossClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final TossClient tossClient;

    public PaymentController(TossClient tossClient) {
        this.tossClient = tossClient;
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@RequestBody PaymentApproveRequest request) {

        // TODO: 여기서 orderId로 우리 DB Order 조회해서 amount 비교, 상태 체크 등 로직 추가 예정
        Map<String, Object> result = tossClient.confirmPayment(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        // TODO: result 기반으로 payment/order 엔티티 저장하는 서비스 계층 호출
        return ResponseEntity.ok(result);
    }
}
