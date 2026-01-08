package com.example.profile_service.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ProfileErrorCode implements ErrorCode {
    UNAUTHENTICATED(401, "Unauthenticated in profile-service!", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(402, "Unauthorized in profile-service!", HttpStatus.BAD_REQUEST),
    PROFILE_EXISTS(200, "Profile Exist", HttpStatus.BAD_REQUEST),
    PROFILE_NOT_EXITS(201, "User profile  Not Exit", HttpStatus.BAD_REQUEST),
    ADDRESS_EXISTS(202, "Address Exist", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXITS(203, "Address Not Exit", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ProfileErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
