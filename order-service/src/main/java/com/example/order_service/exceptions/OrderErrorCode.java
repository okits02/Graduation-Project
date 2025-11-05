package com.example.order_service.exceptions;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_EXISTS(6804, "Orders not exists!", HttpStatus.BAD_REQUEST)
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    OrderErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
