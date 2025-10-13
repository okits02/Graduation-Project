package com.okits02.common_lib.exception;

import org.springframework.http.HttpStatusCode;

public interface ErrorCode {
    int getCode();
    String getMessage();
    HttpStatusCode getHttpStatusCode();
}
