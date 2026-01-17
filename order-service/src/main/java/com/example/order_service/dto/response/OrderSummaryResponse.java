package com.example.order_service.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryResponse(
        String orderId,
        String userId,
        String firstName,
        String lastName,
        LocalDateTime orderDate,
        BigDecimal totalPrice,
        List<ItemSummaryResponse> items
) {
}
