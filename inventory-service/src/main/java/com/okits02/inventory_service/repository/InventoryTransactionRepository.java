package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {
}
