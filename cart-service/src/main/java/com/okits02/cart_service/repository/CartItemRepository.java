package com.okits02.cart_service.repository;

import com.okits02.cart_service.model.Cart;
import com.okits02.cart_service.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.sku = :sku")
    Optional<CartItem> findByCartAndSku(@Param("cart") Cart cart,
                                              @Param("sku") String sku);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.cartItemId = :cartItemId")
    Optional<CartItem> findByIdAndCart(@Param("cart") Cart cart,
                                       @Param("cartItemId") String cartItemId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart AND ci.cartItemId = :cartItemId")
    void deleteByCartItemIdAndCart(@Param("cart") Cart cart,
                                   @Param("cartItemId") String cartItemId);
}
