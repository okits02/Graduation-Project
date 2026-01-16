package com.example.order_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceChangedItem {
    private String sku;
    private BigDecimal oldSellPrice;
    private BigDecimal newSellPrice;
    private BigDecimal oldListPrice;
    private BigDecimal newListPrice;
}
