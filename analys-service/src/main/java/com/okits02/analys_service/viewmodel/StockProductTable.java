package com.okits02.analys_service.viewmodel;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockProductTable {
    String sku;
    String variantName;
    String thumbnail;
    long totalSold;
    BigDecimal totalRevenue;
}
