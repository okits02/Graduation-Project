package com.okits02.analys_service.viewmodel;

public record OrderStatusChartPoint(
        String label,
        long completed,
        long cancelled
) {
}
