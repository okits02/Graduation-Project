package com.okits02.inventory_service.dto.request;

import com.okits02.inventory_service.validator.QuantityConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInItemRequest {
    String productId;
    String productName;
    @QuantityConstraint(message = "Quantity must not null and greater than 0")
    Integer quantity;
    BigDecimal unitCost;
}
