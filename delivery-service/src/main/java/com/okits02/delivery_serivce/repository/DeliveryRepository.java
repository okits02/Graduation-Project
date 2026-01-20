package com.okits02.delivery_serivce.repository;

import com.okits02.delivery_serivce.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, String> {
    Delivery findByOrderId(String orderId);
}
