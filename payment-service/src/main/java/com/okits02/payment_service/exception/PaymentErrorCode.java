package com.okits02.payment_service.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_EXISTS(8604, "Payment not exists!", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    PaymentErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
