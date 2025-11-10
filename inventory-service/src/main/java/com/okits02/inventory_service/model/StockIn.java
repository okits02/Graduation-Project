package com.okits02.inventory_service.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
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
    @Column(name = "reference_code")
    String referenceCode;
    @Column(name = "total_amount")
    BigDecimal totalAmount;
    @Column(name = "note")
    String note;
    LocalDateTime createAt = LocalDateTime.now();

    @OneToMany(mappedBy = "stockIn", cascade = CascadeType.ALL)
    List<StockInItem> items;

    void calculatorTotalAmount(){
        if (items == null || items.isEmpty()) {
            totalAmount = BigDecimal.ZERO;
            return;
        }

        totalAmount = items.stream()
                .peek(StockInItem::calculatorTotalCost) // đảm bảo mỗi item đã tính totalCost
                .map(StockInItem::getTotalCost)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }
    @PrePersist
    @PreUpdate
    public void prePersist() {
        calculatorTotalAmount();
    }
}

