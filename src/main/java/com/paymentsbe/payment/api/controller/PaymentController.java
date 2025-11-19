package com.paymentsbe.payment.api.controller;

import com.paymentsbe.payment.api.dto.PaymentApproveRequest;
import com.paymentsbe.payment.api.dto.PaymentApproveResponse;
import com.paymentsbe.payment.service.PaymentApproveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentApproveService paymentApproveService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentApproveResponse> confirm(
            @RequestBody PaymentApproveRequest request
    ) {
        PaymentApproveResponse response = paymentApproveService.approve(request);
        return ResponseEntity.ok(response);
    }
}
