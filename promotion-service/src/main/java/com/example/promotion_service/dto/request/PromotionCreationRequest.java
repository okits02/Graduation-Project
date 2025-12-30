package com.example.promotion_service.dto.request;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.PromotionKind;
import com.example.promotion_service.enums.UsageType;
import com.example.promotion_service.validator.ValidPromotion;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidPromotion
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionCreationRequest {
    @NotNull
    String name;
    @NotNull
    String descriptions;
    @NotNull
    DiscountType discountType;
    @NotNull
    UsageType usageType;
    @NotNull
    ApplyTo applyTo;
    @NotNull
    PromotionKind promotionKind;
    double discountPercent;
    double fixedAmount;
    int usageLimited;
    int usageLimitPerUser;
    Double minimumOrderPurchaseAmount;
    Double maxDiscountAmount;
    Date startDate;
    Date endDate;
    Boolean active;
    List<String> productId;
    List<String> categoryId;
}
