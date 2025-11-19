package com.paymentsbe.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 (도메인 검증 실패)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: code={}, message={}", errorCode.getCode(), e.getMessage());

        ApiError body = ApiError.of(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(body);
    }

    // Toss PG 오류
    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<ApiError> handlePaymentGatewayException(PaymentGatewayException e) {
        log.warn("PaymentGatewayException: status={}, body={}", e.getStatusCode(), e.getResponseBody());

        ApiError body = ApiError.of(
                ErrorCode.TOSS_API_ERROR,
                e.getResponseBody()
        );

        // HTTP status는 PG에서 준 status 유지
        return ResponseEntity.status(e.getStatusCode()).body(body);
    }

    // 예상하지 못한 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception e) {
        log.error("Unexpected exception", e);

        ApiError body = ApiError.of(ErrorCode.INTERNAL_ERROR, e.getMessage());
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(body);
    }
}
