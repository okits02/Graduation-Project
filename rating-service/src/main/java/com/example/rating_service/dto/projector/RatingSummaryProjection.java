package com.example.rating_service.dto.projector;

import java.math.BigDecimal;

public interface RatingSummaryProjection {
    BigDecimal getAverage();
    Long getTotal();
}