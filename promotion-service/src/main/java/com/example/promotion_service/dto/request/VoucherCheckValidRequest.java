package com.example.promotion_service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherCheckValidRequest {
    List<String> categoryIds;
    List<String> promotionIds;
    BigDecimal totalAmount;
}
