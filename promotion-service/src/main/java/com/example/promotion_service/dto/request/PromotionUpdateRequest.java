package com.example.promotion_service.dto.request;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.UsageType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionUpdateRequest {
    String id;
    String name;
    String campaignId;
    String description;
    DiscountType discountType;
    UsageType usageType;
    ApplyTo applyTo;
    double discountPercent;
    double fixedAmount;
    int usageLimited;
    Double minimumOrderPurchaseAmount;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Boolean active;
    List<String> productId;
    List<String> categoryId;
    List<String> deleteApplyTo;
}
