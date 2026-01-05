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
@Table(
        name = "inventory_transaction",
        indexes = {
                @Index(name = "idx_inventory_sku", columnList = "sku")
        }
)
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    Inventory inventory;
    @Column(nullable = false)
    String sku;

    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    @Column(nullable = false)
    Integer quantity;

    String referenceId;

    @Enumerated(EnumType.STRING)
    ReferenceType referenceType;
    String note;

    LocalDateTime createdAt = LocalDateTime.now();
    public int delta() {
        return transactionType == TransactionType.IN
                ? quantity
                : -quantity;
    }
}
