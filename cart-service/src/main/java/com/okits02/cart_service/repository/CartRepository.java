package com.okits02.cart_service.repository;

import com.okits02.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, String> {
    Cart findByUserId(String userId);
}
