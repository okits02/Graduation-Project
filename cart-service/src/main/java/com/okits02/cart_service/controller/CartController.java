package com.okits02.cart_service.controller;

import com.okits02.cart_service.dto.request.CartDeleteItemRequest;
import com.okits02.cart_service.dto.request.CartItemRequest;
import com.okits02.cart_service.dto.request.CartUpdateRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.cart_service.dto.response.CartItemResponse;
import com.okits02.cart_service.dto.response.CartResponse;
import com.okits02.cart_service.mapper.CartItemMapper;
import com.okits02.cart_service.model.Cart;
import com.okits02.cart_service.model.CartItem;
import com.okits02.cart_service.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @PostMapping("/create-cart")
    public ApiResponse<CartResponse> save(@RequestBody CartItemRequest request){
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .result(cartService.save(request))
                .build();
    }

    @PutMapping("/update-cart")
    public ApiResponse<CartResponse> update(@RequestBody CartUpdateRequest request){
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .result(cartService.update(request))
                .build();
    }

    @DeleteMapping("/delete-items")
    public ApiResponse<CartResponse> delete(@RequestBody CartDeleteItemRequest request){
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .result(cartService.removeItem(request))
                .build();
    }

    @GetMapping("/get-my-cart")
    public ApiResponse<CartResponse> getMyCart(){
        return ApiResponse.<CartResponse>builder()
                .code(200)
                .result(cartService.getCart())
                .build();
    }

    @GetMapping("/internal/get-cart-item/{cartItemId}")
    public ApiResponse<CartItemResponse> getCartItem(@RequestParam String cartItemId){
        return ApiResponse.<CartItemResponse>builder()
                .code(200)
                .result(cartService.getCartItem(cartItemId))
                .build();
    }
}
