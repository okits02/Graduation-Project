package com.okits02.inventory_service.dto.response;

import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockInResponse {
    String supplierName;
    String referenceCode;
    BigDecimal totalAmount;
    String note;
    LocalDateTime createAt = LocalDateTime.now();
    List<StockInItemResponse> items;
}
