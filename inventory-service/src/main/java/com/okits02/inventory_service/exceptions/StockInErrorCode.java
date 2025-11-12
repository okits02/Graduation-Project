package com.okits02.inventory_service.exceptions;

import com.okits02.common_lib.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum StockInErrorCode implements ErrorCode {
    STOCK_IN_NOT_EXISTS(7401, "Stock-in not exists in database", HttpStatus.BAD_REQUEST),
    STOCK_IN_EXISTS_BY_REFERENCE_CODE(7402, "Stock in already exists by  reference code!", HttpStatus.BAD_REQUEST);
    ;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    StockInErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public HttpStatusCode getHttpStatusCode() {
        return null;
    }
}
