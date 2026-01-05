package com.okits02.inventory_service.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "stock_in")
public class StockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "supplier_name")
    String supplierName;
    @Column(name = "reference_code", unique = true, nullable = false)
    String referenceCode;
    @Column(name = "total_amount")
    BigDecimal totalAmount;
    @Column(name = "note")
    String note;
    LocalDateTime createdAt = LocalDateTime.now();


    @OneToMany(
            mappedBy = "stockIn",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<StockInItem> items;


    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        this.totalAmount = items == null ? BigDecimal.ZERO : items.stream().map(
                item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()))
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

