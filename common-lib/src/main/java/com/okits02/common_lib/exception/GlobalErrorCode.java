package com.okits02.common_lib.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum GlobalErrorCode implements ErrorCode {
    UNAUTHENTICATED(40100, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(40300, "Unauthorized!", HttpStatus.FORBIDDEN),
    ;

    GlobalErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
