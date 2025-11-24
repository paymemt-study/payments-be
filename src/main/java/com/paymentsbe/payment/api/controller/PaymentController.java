package com.paymentsbe.payment.api.controller;

import com.paymentsbe.payment.api.dto.*;
import com.paymentsbe.payment.service.PaymentApproveService;
import com.paymentsbe.payment.service.PaymentCancelService;
import com.paymentsbe.payment.service.PaymentFailService;
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
    private final PaymentFailService paymentFailService;
    private final PaymentCancelService paymentCancelService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentApproveResponse> confirm(
            @RequestBody PaymentApproveRequest request
    ) {
        PaymentApproveResponse response = paymentApproveService.approve(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(
            @RequestBody PaymentFailRequest request
    ) {
        paymentFailService.handleFail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<PaymentCancelResponse> cancel(
            @RequestBody PaymentCancelRequest request
    ) {
        PaymentCancelResponse response = paymentCancelService.cancel(request);
        return ResponseEntity.ok(response);
    }
}
