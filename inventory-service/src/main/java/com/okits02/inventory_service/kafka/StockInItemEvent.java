package com.okits02.inventory_service.kafka;

import com.okits02.inventory_service.enums.EventType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInItemEvent {
    String id;
    String sku;
    Integer quantity;
    BigDecimal unitCost;
    BigDecimal totalCost;
}
