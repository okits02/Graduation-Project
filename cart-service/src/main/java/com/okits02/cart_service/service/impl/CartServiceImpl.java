package com.okits02.cart_service.service.impl;

import com.okits02.cart_service.dto.ProductGetVM;
import com.okits02.cart_service.dto.request.CartDeleteItemRequest;
import com.okits02.cart_service.dto.request.CartItemRequest;
import com.okits02.cart_service.dto.request.CartUpdateRequest;
import com.okits02.cart_service.dto.response.CartItemResponse;
import com.okits02.cart_service.dto.response.CartResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.cart_service.exceptions.CartErrorCode;
import com.okits02.cart_service.mapper.CartItemMapper;
import com.okits02.cart_service.model.Cart;
import com.okits02.cart_service.model.CartItem;
import com.okits02.cart_service.repository.CartItemRepository;
import com.okits02.cart_service.repository.CartRepository;
import com.okits02.cart_service.repository.htppClient.ProductClient;
import com.okits02.cart_service.repository.htppClient.UserClient;
import com.okits02.cart_service.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final CartItemMapper cartItemMapper;

    @Override
    public CartResponse save(CartItemRequest request) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

        var userResponse = userClient.getUserId(authHeader);
        if (userResponse == null || userResponse.getCode() != 200) {
            throw new RuntimeException("User does not exist");
        }

        String userId = userResponse.getResult().getUserId();

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = Cart.builder()
                    .userId(userId)
                    .build();
            cart = cartRepository.save(cart);
        }

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }
        var productResponse = productClient.getProductDetails(request.getSku());
        if (productResponse == null || productResponse.getCode() != 200) {
            throw new RuntimeException("Product not exists");
        }

        ProductGetVM productGetVM = productResponse.getResult();
        Optional<CartItem> existingItemOpt =
                cartItemRepository.findByCartAndSku(cart, request.getSku());

        CartItem cartItem;

        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .sku(productGetVM.getSku())
                    .listPrice(productGetVM.getListPrice())
                    .sellPrice(productGetVM.getSellPrice())
                    .quantity(request.getQuantity())
                    .addedAt(LocalDateTime.now())
                    .cart(cart)
                    .build();

            cart.getItems().add(cartItem);
        }

        cartRepository.save(cart);
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .sku(item.getSku())
                        .thumbnail(productGetVM.getThumbnailUrl())
                        .quantity(item.getQuantity())
                        .listPrice(item.getListPrice())
                        .sellPrice(item.getSellPrice())
                        .addedAt(item.getAddedAt())
                        .build()
                )
                .toList();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(items)
                .build();
        }

    @Override
    public CartResponse update(CartUpdateRequest request) {
        String userId = getUserId();

        Cart cart = cartRepository.findByUserId(userId);
        if(cart == null){
            throw new AppException(CartErrorCode.USER_DOES_NOT_HAVE_CART);
        }
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getCartItemId().equals(request.getSku()))
                .findFirst()
                .orElseThrow(() -> new AppException(CartErrorCode.CART_ITEM_NOT_EXISTS));
        cartItem.setQuantity(request.getQuantity());
        cart = cartRepository.save(cart);
        var productResponse = productClient.getProductDetails(request.getSku());
        if (productResponse == null || productResponse.getCode() != 200) {
            throw new RuntimeException("Product not exists");
        }

        ProductGetVM productGetVM = productResponse.getResult();
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .sku(request.getSku())
                        .variantName(productGetVM.getVariantName())
                        .thumbnail(productGetVM.getThumbnailUrl())
                        .quantity(item.getQuantity())
                        .listPrice(item.getListPrice())
                        .sellPrice(item.getSellPrice())
                        .addedAt(item.getAddedAt())
                        .build()
                )
                .collect(Collectors.toList());
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(cartItemResponses)
                .build();
    }

    @Override
    public CartResponse removeItem(CartDeleteItemRequest request) {
        String userId = getUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if(cart == null){
            throw new AppException(CartErrorCode.USER_DOES_NOT_HAVE_CART);
        }
        cart.getItems().removeIf(item -> request.getCartItemId().contains(item.getCartItemId()));
        cart = cartRepository.save(cart);
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .quantity(item.getQuantity())
                        .listPrice(item.getListPrice())
                        .sellPrice(item.getSellPrice())
                        .addedAt(item.getAddedAt())
                        .build()
                )
                .collect(Collectors.toList());
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(cartItemResponses)
                .build();
    }

    @Override
    public CartResponse getCart() {
        String userId = getUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return CartResponse.builder()
                    .cartId(cart != null ? cart.getCartId() : null)
                    .items(List.of())
                    .build();
        }
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> {

                    ProductGetVM product = null;

                    var productResponse = productClient.getProductDetails(item.getSku());
                    if (productResponse != null && productResponse.getCode() == 200) {
                        product = productResponse.getResult();
                    }

                    return CartItemResponse.builder()
                            .cartItemId(item.getCartItemId())
                            .sku(item.getSku())
                            .thumbnail(product != null ? product.getThumbnailUrl() : null)
                            .quantity(item.getQuantity())
                            .listPrice(item.getListPrice())
                            .sellPrice(item.getSellPrice())
                            .addedAt(item.getAddedAt())
                            .build();
                })
                .toList();

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .items(cartItemResponses)
                .build();
    }

    @Override
    public CartItemResponse getCartItem(String cartItemId) {
        String userId = getUserId();
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new AppException(CartErrorCode.USER_DOES_NOT_HAVE_CART);
        }

        Optional<CartItem> cartItemOpt =
                cartItemRepository.findByIdAndCart(cart, cartItemId);

        if (cartItemOpt.isEmpty()) {
            throw new AppException(CartErrorCode.CART_ITEM_NOT_EXISTS);
        }
        CartItem cartItem = cartItemOpt.get();
        ProductGetVM product = null;
        var productResponse = productClient.getProductDetails(cartItem.getSku());
        if (productResponse != null && productResponse.getCode() == 200) {
            product = productResponse.getResult();
        }

        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .sku(cartItem.getSku())
                .thumbnail(product != null ? product.getThumbnailUrl() : null)
                .quantity(cartItem.getQuantity())
                .listPrice(cartItem.getListPrice())
                .sellPrice(cartItem.getSellPrice())
                .addedAt(cartItem.getAddedAt())
                .build();
    }

    private String getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var userResponse = userClient.getUserId(authHeader);
        if (userResponse == null || userResponse.getCode() != 200) {
            throw new RuntimeException("User does not exist");
        }
        return userResponse.getResult().getUserId();
    }

}
