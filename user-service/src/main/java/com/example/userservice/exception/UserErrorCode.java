package com.example.userservice.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {
    USER_EXISTS(2002, "User Exist", HttpStatus.CONFLICT),
    USER_NOT_EXISTS(2004, "User Not Exist", HttpStatus.NOT_FOUND),
    PASSWORD_NOT_MATCH(2005, "Password Not Match", HttpStatus.BAD_REQUEST),
    OTP_NOT_EXISTS(2007, "OTP Not Exist", HttpStatus.NOT_FOUND),
    EMAIL_EXISTS(2008, "Email Exist", HttpStatus.CONFLICT),
    OTP_INVALID(2009, "OTP is not valid!", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(2010, "OTP has expired!", HttpStatus.BAD_REQUEST),
    CAN_NOT_CONNECT_TO_PROFILE(2011, "Can not connected to profile client", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    UserErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }

    @Override
    public HttpStatus getHttpStatusCode() { return httpStatus; }
}
