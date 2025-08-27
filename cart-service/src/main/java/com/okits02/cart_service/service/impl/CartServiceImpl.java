package com.okits02.cart_service.service.impl;

import com.okits02.cart_service.dto.PageResponse;
import com.okits02.cart_service.dto.ProductGetVM;
import com.okits02.cart_service.dto.request.CartDeleteItemRequest;
import com.okits02.cart_service.dto.request.CartItemRequest;
import com.okits02.cart_service.dto.request.CartUpdateRequest;
import com.okits02.cart_service.dto.response.CartItemResponse;
import com.okits02.cart_service.dto.response.CartResponse;
import com.okits02.cart_service.exceptions.AppException;
import com.okits02.cart_service.exceptions.ErrorCode;
import com.okits02.cart_service.mapper.CartItemMapper;
import com.okits02.cart_service.mapper.CartMapper;
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
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.*;
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
            CartItem cartItem = createItems(authHeader, cart, request.getProductId(), request.getQuantity());
            cart = cartRepository.save(cart);
            List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                    .map(item -> CartItemResponse.builder()
                            .cartItemId(item.getCartItemId())
                            .productId(item.getProductId())
                            .productName(item.getProductName())
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
    public CartResponse update(CartUpdateRequest request) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

        var userResponse = userClient.getUserId(authHeader);
        if (userResponse == null || userResponse.getCode() != 200) {
            throw new RuntimeException("User does not exist");
        }

        String userId = userResponse.getResult().getUserId();

        Cart cart = cartRepository.findByUserId(userId);
        if(cart == null){
            throw new AppException(ErrorCode.USER_DOES_NOT_HAVE_CART);
        }
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getCartItemId().equals(request.getCartItemId()))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXISTS));
        cartItem.setQuantity(request.getQuantity());
        cart = cartRepository.save(cart);
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
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
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

        var userResponse = userClient.getUserId(authHeader);
        if (userResponse == null || userResponse.getCode() != 200) {
            throw new RuntimeException("User does not exist");
        }

        String userId = userResponse.getResult().getUserId();

        Cart cart = cartRepository.findByUserId(userId);
        if(cart == null){
            throw new AppException(ErrorCode.USER_DOES_NOT_HAVE_CART);
        }
        cart.getItems().removeIf(item -> request.getCartItemId().contains(item.getCartItemId()));
        cart = cartRepository.save(cart);
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
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
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");

        var userResponse = userClient.getUserId(authHeader);
        if (userResponse == null || userResponse.getCode() != 200) {
            throw new RuntimeException("User does not exist");
        }

        String userId = userResponse.getResult().getUserId();

        Cart cart = cartRepository.findByUserId(userId);
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
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

    private CartItem createItems(String token, Cart cart, String productId, Integer quantity){
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProductId(cart, productId);
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cart.getItems().add(cartItem);
            return cartItem;
        }
        var response = productClient.getProductDetails(token, productId);
        if(response == null || response.getBody().getCode() != 200){
            throw new RuntimeException("Product not exists");
        }
        ProductGetVM productGetVM = response.getBody().getResult();

        CartItem cartItem = CartItem.builder()
                .productId(productGetVM.getId())
                .productName(productGetVM.getName())
                .listPrice(productGetVM.getListPrice())
                .sellPrice(productGetVM.getSellPrice())
                .quantity(quantity)
                .addedAt(LocalDateTime.now())
                .cart(cart)
                .build();
        cart.getItems().add(cartItem);
        return cartItem;
    }
}
