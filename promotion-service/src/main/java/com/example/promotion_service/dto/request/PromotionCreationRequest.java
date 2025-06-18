package com.example.promotion_service.dto.request;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.UsageType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionCreationRequest {
    @NotNull
    String name;
    @NotNull
    String description;
    @NotNull
    DiscountType discountType;
    @NotNull
    UsageType usageType;
    @NotNull
    ApplyTo applyTo;
    double discountPercent;
    double fixedAmount;
    int usageLimited;
    Double minimumOrderPurchaseAmount;
    Date startDate;
    Date endDate;
    boolean isActive;
    List<String> productId;
    List<String> categoryId;
    boolean isVoucher;
}
