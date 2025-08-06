package com.example.rating_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNAUTHENTICATED(HttpStatus.BAD_REQUEST, 4001, "Unauthenticate in promotion-service!"),
    RATING_EXISTS(HttpStatus.BAD_REQUEST, 4020, "You have rated of product")
    ;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    private int code;
    private String message;
    private HttpStatus httpStatus;
}
