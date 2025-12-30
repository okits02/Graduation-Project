package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {
    Inventory findByProductId(String productId);
    Boolean existsByProductId(String productId);

    Optional<Inventory> findByProductIdAndSku(String productId, String sku);

    List<Inventory> findAllByProductId(String productId);

    boolean existsBySkuAndQuantityGreaterThan(String sku, Integer quantity);

    Page<Inventory> findAllByProductId(String productId, Pageable pageable);
}
