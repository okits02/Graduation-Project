package com.example.product_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    PRODUCT_EXISTS(1002, "Product exists!", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTS(1004, "Product not exists!", HttpStatus.BAD_REQUEST),
    CATE_NOT_EXISTS(1006, "Category not exists!", HttpStatus.BAD_REQUEST),
    CATE_EXISTS(1008, "Cate exists!", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
