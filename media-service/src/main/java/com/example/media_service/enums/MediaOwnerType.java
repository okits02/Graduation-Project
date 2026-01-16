package com.example.media_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MediaOwnerType {

    PRODUCT("PRODUCT", "Sản phẩm"),
    CATEGORY("CATEGORY", "Danh mục"),
    PRODUCT_VARIANT("PRODUCT_VARIANT", "Biến thể sản phẩm"),
    BRAND("BRAND", "Thương hiệu"),
    USER("USER", "Người dùng"),
    PROMOTION("PROMOTION", "Chương trình khuyến mãi"),
    RATING("RATING", "Đánh giá");

    private final String code;
    private final String label;

    MediaOwnerType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @JsonCreator
    public static MediaOwnerType fromCode(String code) {
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid MediaOwnerType: " + code));
    }
}