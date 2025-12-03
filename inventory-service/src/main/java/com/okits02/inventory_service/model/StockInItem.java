package com.okits02.inventory_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "stock_in_item")
public class StockInItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "product_id")
    String productId;
    @Column(name = "product_name")
    String productName;
    @Column(name = "quantity")
    Integer quantity;
    @Column(name = "unit_cost")
    BigDecimal unitCost;
    @Column(name = "total_cost")
    BigDecimal totalCost;

    @ManyToOne
    @JoinColumn(name = "stock_in_id")
    StockIn stockIn;

    void calculatorTotalCost(){
        if (unitCost == null || quantity == null) {
            totalCost = BigDecimal.ZERO;
        } else {
            totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        calculatorTotalCost();
    }
}
