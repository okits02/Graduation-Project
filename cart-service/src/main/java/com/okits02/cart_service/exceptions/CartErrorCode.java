package com.okits02.cart_service.exceptions;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum CartErrorCode implements ErrorCode {
    USER_DOES_NOT_HAVE_CART(40401, "User does not have a cart", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_EXISTS(40501, "Item does not exists in cart", HttpStatus.BAD_REQUEST);
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    CartErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
