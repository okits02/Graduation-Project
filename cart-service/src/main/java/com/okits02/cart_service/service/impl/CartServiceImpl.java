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
import com.okits02.cart_service.repository.htppClient.SearchClient;
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
    private final SearchClient searchClient;

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
        var productResponse =
                searchClient.getProductDetails(List.of(request.getSku()));

        if (productResponse == null
                || productResponse.getCode() != 200
                || productResponse.getResult().isEmpty()) {
            throw new RuntimeException("Product not exists");
        }

        ProductGetVM productGetVM = productResponse.getResult().get(0);
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
                        .variantName(productGetVM.getVariantName())
                        .thumbnail(productGetVM.getThumbnailUrl())
                        .quantity(item.getQuantity())
                        .listPrice(item.getListPrice())
                        .sellPrice(item.getSellPrice())
                        .promotionName(productGetVM.getPromotionName())
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
                .filter(item -> item.getSku().equals(request.getSku()))
                .findFirst()
                .orElseThrow(() -> new AppException(CartErrorCode.CART_ITEM_NOT_EXISTS));
        cartItem.setQuantity(request.getQuantity());
        cart = cartRepository.save(cart);
        var productResponse =
                searchClient.getProductDetails(List.of(request.getSku()));

        if (productResponse == null
                || productResponse.getCode() != 200
                || productResponse.getResult().isEmpty()) {
            throw new RuntimeException("Product not exists");
        }

        ProductGetVM productGetVM = productResponse.getResult().get(0);
        List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .sku(item.getSku())
                        .variantName(productGetVM.getVariantName())
                        .thumbnail(productGetVM.getThumbnailUrl())
                        .quantity(item.getQuantity())
                        .listPrice(item.getListPrice())
                        .sellPrice(item.getSellPrice())
                        .promotionName(productGetVM.getPromotionName())
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
    public void removeItem(CartDeleteItemRequest request) {
        String userId = getUserId();

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new AppException(CartErrorCode.USER_DOES_NOT_HAVE_CART);
        }

        Iterator<CartItem> iterator = cart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (request.getCartItemId().contains(item.getCartItemId())) {
                iterator.remove();
                item.setCart(null);
            }
        }

        cartRepository.save(cart);
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

                    var productResponse =
                            searchClient.getProductDetails(List.of(item.getSku()));

                    if (productResponse == null
                            || productResponse.getCode() != 200
                            || productResponse.getResult().isEmpty()) {
                        throw new RuntimeException("Product not exists");
                    }

                    product = productResponse.getResult().get(0);

                    return CartItemResponse.builder()
                            .cartItemId(item.getCartItemId())
                            .sku(item.getSku())
                            .thumbnail(product != null ? product.getThumbnailUrl() : null)
                            .variantName(product.getVariantName())
                            .promotionName(product.getPromotionName())
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
        var productResponse =
                searchClient.getProductDetails(List.of(cartItem.getSku()));

        if (productResponse != null
                && productResponse.getCode() == 200
                && !productResponse.getResult().isEmpty()) {
            product = productResponse.getResult().get(0);
        }

        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .sku(cartItem.getSku())
                .variantName(product.getVariantName())
                .thumbnail(product != null ? product.getThumbnailUrl() : null)
                .promotionName(product.getPromotionName())
                .quantity(cartItem.getQuantity())
                .listPrice(cartItem.getListPrice())
                .sellPrice(cartItem.getSellPrice())
                .addedAt(cartItem.getAddedAt())
                .build();
    }

    @Override
    public void removeItemByUserId(String userId, String sku) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            throw new AppException(CartErrorCode.USER_DOES_NOT_HAVE_CART);
        }
        boolean removed = cart.getItems()
                .removeIf(item -> sku.equals(item.getSku()));

        if (!removed) {
            throw new AppException(CartErrorCode.CART_ITEM_NOT_EXISTS);
        }

        cartRepository.save(cart);
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

    private Map<String, ProductGetVM> fetchProductsBySku(List<String> skus) {

        if (skus == null || skus.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            var response = searchClient.getProductDetails(skus);

            if (response == null || response.getResult() == null) {
                return Collections.emptyMap();
            }

            return response.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductGetVM::getSku,
                            Function.identity()
                    ));

        } catch (Exception e) {
            log.warn("Cannot fetch products by skus {}", skus, e);
            return Collections.emptyMap();
        }
    }

}
