package com.example.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_EXISTS(2002, "User Exist", HttpStatus.BAD_REQUEST),
    USER_NOT_EXITS(2004, "User Not Exit", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTS(2003, "Role Not Exist", HttpStatus.BAD_REQUEST),
    PASSWORD_NOT_MATCH(2005, "Password Not Match", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(40100, "Unauthenticated!", HttpStatus.UNAUTHORIZED),;
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
