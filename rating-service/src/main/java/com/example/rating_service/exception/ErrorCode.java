package com.example.rating_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNAUTHENTICATED(HttpStatus.BAD_REQUEST, 4001, "Unauthenticate in promotion-service!"),
    PROMOTION_EXISTS(HttpStatus.BAD_REQUEST, 4002, "Promotion is exists!"),
    PROMOTION_NOT_EXISTS(HttpStatus.BAD_REQUEST, 4007, "Can not find promotion with promotionId"),
    INVALID_PRODUCT_IDS(HttpStatus.BAD_REQUEST, 4003, "Invalid product id!"),
    INVALID_CATEGORY_IDS(HttpStatus.BAD_REQUEST, 4004, "Invalid category id!"),
    CAN_NOT_CREATE_VOUCHER(HttpStatus.BAD_REQUEST, 4005, "Can't create voucher"),
    USAGE_LIMITED_NULL(HttpStatus.BAD_REQUEST, 4006, "Usage limited can not null!")
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
