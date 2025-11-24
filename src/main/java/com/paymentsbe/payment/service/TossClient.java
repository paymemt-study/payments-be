package com.paymentsbe.payment.service;

import com.paymentsbe.common.exception.PaymentGatewayException;
import com.paymentsbe.common.config.TossProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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

        try {
            return tossRestClient.post()
                    .uri("/payments/confirm")
                    .header("Authorization", basicAuthHeader())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        }
    }

    /**
     * 카드 결제 취소(전액/부분 환불)
     */
    public Map<String, Object> cancelPayment(String paymentKey, Long cancelAmount, String reason) {

        Map<String, Object> requestBody = Map.of(
                "cancelReason", reason != null ? reason : "USER_REQUEST",
                "cancelAmount", cancelAmount
        );

        try {
            return tossRestClient.post()
                    .uri("/payments/{paymentKey}/cancel", paymentKey)
                    .header("Authorization", basicAuthHeader())
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
        }
    }
}
