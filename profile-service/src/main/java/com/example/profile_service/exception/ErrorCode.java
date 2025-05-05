package com.example.profile_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    PROFILE_EXISTS(2000, "Profile Exist", HttpStatus.BAD_REQUEST),
    PROFILE_NOT_EXITS(2011, "User Not Exit", HttpStatus.BAD_REQUEST),
    ADDRESS_EXISTS(2012, "Address Exist", HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXITS(2013, "Address Not Exit", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
