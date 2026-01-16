package com.okits02.inventory_service.repository;

import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import com.okits02.inventory_service.model.InventoryTransaction;
import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {
    Page<InventoryTransaction> findBySkuOrderByCreatedAtDesc(
            String sku,
            Pageable pageable
    );

    @Query("""
    SELECT it.sku, COALESCE(SUM(it.quantity), 0)
    FROM InventoryTransaction it
    WHERE it.sku IN :skus
      AND it.transactionType = :type
      AND it.referenceType = :ref
    GROUP BY it.sku
    """)
    List<Object[]> sumSoldGroupBySku(
            @Param("skus") List<String> skus,
            @Param("type") TransactionType type,
            @Param("ref") ReferenceType ref
    );
}
