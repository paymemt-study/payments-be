package com.paymentsbe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 주문 관련
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "AMOUNT_MISMATCH", "결제 금액이 주문 금액과 일치하지 않습니다."),
    ALREADY_PROCESSED(HttpStatus.CONFLICT, "ALREADY_PROCESSED", "이미 처리된 주문입니다."),
    CANNOT_CANCEL_ORDER(HttpStatus.BAD_REQUEST,"CANNOT_CANCEL_ORDER", "취소할 수 없는 주문 상태입니다."),

    // 결제 관련
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", "결제를 찾을 수 없습니다."),
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_REFUND_AMOUNT", "유효하지 않은 환불 금액입니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_STATUS", "유효하지 않은 결제 상태입니다."),

    // PG(Toss) 연동 에러
    TOSS_API_ERROR(HttpStatus.BAD_GATEWAY, "TOSS_API_ERROR", "결제 대행사(Toss) 처리 중 오류가 발생했습니다."),

    // 공통
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
