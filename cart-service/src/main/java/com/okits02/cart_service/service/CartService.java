package com.okits02.cart_service.service;

import com.okits02.cart_service.dto.request.CartDeleteItemRequest;
import com.okits02.cart_service.dto.request.CartItemRequest;
import com.okits02.cart_service.dto.request.CartUpdateRequest;
import com.okits02.cart_service.dto.response.CartItemResponse;
import com.okits02.cart_service.dto.response.CartResponse;
import com.okits02.cart_service.model.Cart;

import java.util.List;

public interface CartService {
    public CartResponse save(CartItemRequest request);
    public CartResponse update(CartUpdateRequest request);
    public void removeItem(CartDeleteItemRequest request);
    public CartResponse getCart();
    public CartItemResponse getCartItem(String cartItemId);
}
