package com.paymentsbe.payment.api.controller;

import com.paymentsbe.payment.api.dto.TossWebhookPayload;
import com.paymentsbe.payment.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/webhook")
public class PaymentWebhookController {

    private final PaymentWebhookService paymentWebhookService;

    @PostMapping("/toss")
    public ResponseEntity<Void> webhook(@RequestBody TossWebhookPayload payload) {
        paymentWebhookService.handleWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
