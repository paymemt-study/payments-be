package com.paymentsbe.common.exception;

import lombok.Getter;

@Getter
public class PaymentGatewayException extends RuntimeException {

    private final int statusCode;      // Toss에서 내려준 HTTP status
    private final String responseBody; // Toss에서 내려준 body

    public PaymentGatewayException(int statusCode, String responseBody) {
        super("Payment gateway error. status=" + statusCode);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
