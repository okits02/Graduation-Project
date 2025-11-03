package com.okits02.inventory_service.exceptions;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum InventoryErrorCode implements ErrorCode {
    PRODUCT_EXISTS(4002, "Product exists on inventory!", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTS(4004, "Product not exists in inventory!", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_ENOUGH(4005, "The number of products is not enough", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    InventoryErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
/*The number of products is not enough*/