package com.example.rating_service.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum RatingErrorCode implements ErrorCode {
    UNAUTHENTICATED(HttpStatus.BAD_REQUEST, 4001, "Unauthenticate in promotion-service!"),
    RATING_EXISTS(HttpStatus.BAD_REQUEST, 4020, "You have rated of product")
    ;

    RatingErrorCode(HttpStatus httpStatus, int code, String message) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatus;
    }

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
