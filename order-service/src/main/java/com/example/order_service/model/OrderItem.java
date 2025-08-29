package com.example.order_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders orders;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "name_product")
    private String productName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "list_price", precision = 10, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "sell_price", precision = 10, scale = 2)
    private BigDecimal sellPrice;

    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;


    public void calculatorSellPrice(){
        if(sellPrice == null || quantity == null) return;
        totalPrice = sellPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
