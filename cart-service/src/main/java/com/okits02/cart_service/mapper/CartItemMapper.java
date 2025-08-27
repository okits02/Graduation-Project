package com.okits02.cart_service.mapper;

import com.okits02.cart_service.dto.response.CartItemResponse;
import com.okits02.cart_service.model.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemResponse toCartItemResponse(CartItem cartItem);
}
