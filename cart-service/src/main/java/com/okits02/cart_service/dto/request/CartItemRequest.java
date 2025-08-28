package com.okits02.cart_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemRequest {
    String productId;
    Integer quantity;
}
