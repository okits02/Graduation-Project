package com.example.promotion_service.dto.request;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.PromotionKind;
import com.example.promotion_service.enums.UsageType;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashSaleCreationRequest {
    @NotNull
    String name;
    @NotNull
    String descriptions;
    @NotNull
    UsageType usageType;
    @NotNull
    ApplyTo applyTo;
    @NotNull
    PromotionKind promotionKind;
    LocalDate startDate;
    LocalDate endDate;
    Boolean active;
    List<FlashSaleItemRequest> flashSaleItemRequests;
}
