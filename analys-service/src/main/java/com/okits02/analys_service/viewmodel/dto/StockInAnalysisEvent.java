package com.okits02.analys_service.viewmodel.dto;

import com.okits02.analys_service.enums.EventType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
