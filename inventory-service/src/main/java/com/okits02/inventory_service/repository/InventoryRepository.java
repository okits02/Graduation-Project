package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.Inventory;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findBySku(String sku);


    boolean existsBySkuAndQuantityGreaterThan(String sku, Integer quantity);

    @Query("""
    SELECT i
    FROM Inventory i
    WHERE i.sku IN :skus
    """)
    List<Inventory> findBySkus(@Param("skus") List<String> skus);
}
