package com.okits02.analys_service.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class RevenueStockChartPoint {
    private LocalDate date;
    private BigDecimal totalSales;
    private BigDecimal totalStockIn;
}
