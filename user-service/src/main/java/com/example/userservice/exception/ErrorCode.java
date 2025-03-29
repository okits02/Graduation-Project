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
    UNAUTHENTICATED(40100, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(40300, "Unauthorized!", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACTION(40400, "Unauthorized Action", HttpStatus.UNAUTHORIZED),
    ADDRESS_NOT_EXISTS(2006, "Address Not Exist", HttpStatus.BAD_REQUEST),
    OTP_NOT_EXISTS(2007, "OTP Not Exist", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTS(2008, " Email Exist" , HttpStatus.BAD_REQUEST),
    OTP_INVALID(2009, "OTP is no valid!", HttpStatus.BAD_REQUEST),;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;
}
