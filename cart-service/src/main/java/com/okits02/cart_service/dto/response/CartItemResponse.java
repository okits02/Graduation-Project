package com.okits02.cart_service.dto.response;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemResponse {
    private String cartItemId;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal listPrice;
    private BigDecimal sellPrice;
    private LocalDateTime addedAt;

}
