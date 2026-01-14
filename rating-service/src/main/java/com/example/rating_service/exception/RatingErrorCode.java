package com.example.rating_service.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum RatingErrorCode implements ErrorCode {
    UNAUTHENTICATED(HttpStatus.BAD_REQUEST, 4001, "Unauthenticate in promotion-service!"),
    RATING_EXISTS(HttpStatus.BAD_REQUEST, 4020, "You have rated of product"),
    USER_CAN_NOT_DELETE_COMMENT(HttpStatus.BAD_REQUEST, 40206, "You can not delete comments that are not yours"),
    COMMENT_NOT_EXISTS(HttpStatus.BAD_REQUEST, 4021, "Comment not exists!"),
    PROFILE_NOT_EXISTS(HttpStatus.BAD_REQUEST, 4022, "Failed to call profile form profile-service"),
    ADMIN_ONLY_REPLY_COMMENT(HttpStatus.BAD_REQUEST,403, "Admin is only allowed to reply to existing comments"),
    ADMIN_CANNOT_RATE(HttpStatus.BAD_REQUEST,403, "Admin is not allowed to create product ratings"),
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
