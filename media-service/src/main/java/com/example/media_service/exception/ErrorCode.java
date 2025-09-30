package com.example.media_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNAUTHENTICATED(1401, "Unauthenticated in media-service!", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1402, "Unauthorized in media-service!", HttpStatus.BAD_REQUEST),
    CAN_NOT_FIND_MEDIA_BY_PRODUCT(1403, "Can not find media by productId!", HttpStatus.BAD_REQUEST),
    CAN_NOT_FIND_MEDIA_BY_URL(1404, "Can not find media by url", HttpStatus.BAD_REQUEST),
    THUMBNAIL_EXISTS(1405, "Thumbnail photo still exists!", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
