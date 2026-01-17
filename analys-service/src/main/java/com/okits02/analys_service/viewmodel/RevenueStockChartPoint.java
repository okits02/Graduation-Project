package com.okits02.analys_service.viewmodel;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;


public record RevenueStockChartPoint (
        String label,
        BigDecimal revenue,
        BigDecimal stockIn
){
}
