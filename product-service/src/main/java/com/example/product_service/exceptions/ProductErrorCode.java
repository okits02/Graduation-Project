package com.example.product_service.exceptions;

import com.okits02.common_lib.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ProductErrorCode implements ErrorCode {
    UNAUTHENTICATED(40100, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    PRODUCT_EXISTS(1002, "Product exists!", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_EXISTS(1004, "Product not exists!", HttpStatus.BAD_REQUEST),
    CATE_NOT_EXISTS(1006, "Category not exists!", HttpStatus.BAD_REQUEST),
    CATE_EXISTS(1008, "Cate exists!", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_EXISTS(1010, "Image is not exists", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY_LEVLE(1001, "Cannot apply promotion to parent & childrent categories",
            HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_WITH_CHILDREN(1012, "Can not delete with childrent", HttpStatus.BAD_REQUEST),
    BRAND_EXISTS(1014, "Brand exists!", HttpStatus.BAD_REQUEST),
    BRAND_NOT_EXISTS(1016, "BRAND not exists!", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANTS_EXISTS(1018, "product_variants exists!", HttpStatus.BAD_REQUEST),
    PRODUCT_VARIANTS_NOT_FOUND(1020, "product_variants not found", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_VARIANT(1022, "product_variants invalid in product!", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_UNDER_ROOT(1024, "Category id in request not under root!", HttpStatus.BAD_REQUEST);
    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ProductErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
