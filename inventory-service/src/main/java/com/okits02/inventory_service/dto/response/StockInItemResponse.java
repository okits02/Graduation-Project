package com.okits02.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInItemResponse {
    String productId;
    String sku;
    String productName;
    Integer quantity;
    BigDecimal unitCost;
    BigDecimal totalCost;
}
