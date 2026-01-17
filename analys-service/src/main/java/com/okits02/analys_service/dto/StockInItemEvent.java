package com.okits02.analys_service.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInItemEvent {
    String id;
    String sku;
    String variantName;
    String thumbnail;
    Integer quantity;
    BigDecimal unitCost;
    BigDecimal totalCost;
}
