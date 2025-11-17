package com.okits02.inventory_service.model;

import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "inventory_transaction")
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
    TransactionType transactionType;

    @Column(nullable = false)
    Integer quantity;

    String referenceId;

    @Enumerated
    ReferenceType referenceType;
    String note;

    LocalDateTime createdAt = LocalDateTime.now();
}
