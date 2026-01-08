package com.example.media_service.exception;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum MediaErrorCode implements ErrorCode {
    UNAUTHENTICATED(401, "Unauthenticated in media-service!", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(402, "Unauthorized in media-service!", HttpStatus.BAD_REQUEST),
    CAN_NOT_FIND_MEDIA_BY_PRODUCT(403, "Can not find media by productId!", HttpStatus.BAD_REQUEST),
    CAN_NOT_FIND_MEDIA_BY_URL(404, "Can not find media by url", HttpStatus.BAD_REQUEST),
    THUMBNAIL_EXISTS(405, "Thumbnail photo still exists!", HttpStatus.BAD_REQUEST),
    CAN_NOT_FIND_MEDIA_BY_ID(406, "Can not find media by Id", HttpStatus.BAD_REQUEST),
    BANNER_IS_NOT_EXISTS(408, "Can not find another banner in system", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    MediaErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
