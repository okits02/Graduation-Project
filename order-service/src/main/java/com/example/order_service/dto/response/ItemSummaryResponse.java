package com.example.order_service.dto.response;

import java.math.BigDecimal;

public record ItemSummaryResponse(
        String productName,
        String thumbnailUrl,
        BigDecimal sellPrice,
        BigDecimal listPrice,
        Integer quantity) {
}
