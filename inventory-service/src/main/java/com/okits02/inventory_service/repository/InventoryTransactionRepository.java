package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {
    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(
            String productId,
            Pageable pageable
    );
}
