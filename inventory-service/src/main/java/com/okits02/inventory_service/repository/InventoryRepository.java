package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    Inventory findByProductId(String productId);
    Boolean existsByProductId(String productId);
}
