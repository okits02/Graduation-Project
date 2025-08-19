package com.okits02.cart_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHENTICATED(40300, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    PRODUCT_EXISTS(40100, "Product is exists in search!", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTS(40200, "Product is not exists in search!", HttpStatus.BAD_REQUEST),
    ID_OF_PROMOTION_NOT_VALID(40600, "Promotion id on message is Empty!", HttpStatus.BAD_REQUEST)
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
