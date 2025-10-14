package com.example.promotion_service.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum PromotionErrorCode implements ErrorCode {
    UNAUTHENTICATED(HttpStatus.BAD_REQUEST, 4001, "Unauthenticate in promotion-service!"),
    PROMOTION_EXISTS(HttpStatus.BAD_REQUEST, 4002, "Promotion is exists!"),
    PROMOTION_NOT_EXISTS(HttpStatus.BAD_REQUEST, 4007, "Can not find promotion with promotionId"),
    INVALID_PRODUCT_IDS(HttpStatus.BAD_REQUEST, 4003, "Invalid product id!"),
    INVALID_CATEGORY_IDS(HttpStatus.BAD_REQUEST, 4004, "Invalid category id!"),
    CAN_NOT_CREATE_VOUCHER(HttpStatus.BAD_REQUEST, 4005, "Can't create voucher"),
    USAGE_LIMITED_NULL(HttpStatus.BAD_REQUEST, 4006, "Usage limited can not null!")
    ;

    PromotionErrorCode(HttpStatus httpStatus, int code, String message) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatus;
    }

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;

}
