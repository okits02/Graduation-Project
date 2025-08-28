package com.okits02.cart_service.dto.response;

import com.okits02.cart_service.model.CartItem;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartResponse {
    private String cartId;
    private List<CartItemResponse> items = new ArrayList<>();
}
