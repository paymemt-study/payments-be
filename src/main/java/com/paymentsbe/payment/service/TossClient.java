package com.paymentsbe.payment.service;

import com.paymentsbe.config.TossProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class TossClient {

    private final RestClient tossRestClient;
    private final TossProperties tossProperties;

    public TossClient(RestClient tossRestClient, TossProperties tossProperties) {
        this.tossRestClient = tossRestClient;
        this.tossProperties = tossProperties;
    }

    private String basicAuthHeader() {
        String raw = tossProperties.secretKey() + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    public Map<String, Object> confirmPayment(String paymentKey, String orderId, Long amount) {

        Map<String, Object> requestBody = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        return tossRestClient.post()
                .uri("/payments/confirm")
                .header("Authorization", basicAuthHeader())
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});// 처음엔 Map으로 받고, 나중에 DTO로 바꿔도 됨
    }
}