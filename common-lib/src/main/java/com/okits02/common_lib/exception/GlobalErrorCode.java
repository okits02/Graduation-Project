package com.okits02.common_lib.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum GlobalErrorCode implements ErrorCode {
    UNAUTHENTICATED(40100, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(40300, "Unauthorized!", HttpStatus.FORBIDDEN),
    SERVICE_TIMEOUT(5001, "Request timeout", HttpStatus.REQUEST_TIMEOUT),
    SERVICE_UNAVAILABLE(5002, "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR(5000, "Internal system error", HttpStatus.INTERNAL_SERVER_ERROR);
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
