package com.okits02.inventory_service.model;

import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "inventory-transaction")
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    Inventory inventory;

    @Column(nullable = false)
    String productId;

    @Enumerated
    TransactionType transactionType;  // IN / OUT / RETURN / ADJUST

    @Column(nullable = false)
    Integer quantity;

    String referenceId;      // Liên kết đến StockIn, Order, Return...

    @Enumerated
    ReferenceType referenceType;    // STOCK_IN / ORDER / RETURN / MANUAL
    String note;

    LocalDateTime createdAt = LocalDateTime.now();
}
