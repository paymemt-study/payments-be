package com.paymentsbe.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiError {

    private final int status;
    private final String code;
    private final String message;
    private final String detail;
    private final Instant timestamp;

    public static ApiError of(ErrorCode errorCode) {
        return ApiError.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(null)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiError of(ErrorCode errorCode, String detail) {
        return ApiError.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .timestamp(Instant.now())
                .build();
    }

    public static ApiError of(String code, String message) {
        return ApiError.builder()
                .status(500)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
