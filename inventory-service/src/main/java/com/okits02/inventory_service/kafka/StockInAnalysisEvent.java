package com.okits02.inventory_service.kafka;

import com.okits02.inventory_service.enums.EventType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInAnalysisEvent {
    EventType eventType;
    String id;
    String supplierName;
    String referenceCode;
    BigDecimal totalAmount;
    LocalDateTime createdAt;
    List<StockInItemEvent> items;
}
