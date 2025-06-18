package com.example.promotion_service.dto.response;

import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.UsageType;
import com.example.promotion_service.model.PromotionApplyTo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Builder
@Setter
@Getter
public class PromotionResponse {
    String name;
    DiscountType discountType;
    ApplyTo applyTo;
    UsageType usageType;
    String voucherCode;
    String descriptions;
    Double discountPercent;
    Double fixedAmount;
    Date startDate;
    Date endDate;
    Double minimumOrderPurchaseAmount;
    int usageLimited;
    int usageCount;
    boolean isActive;
    PromotionApplyTo promotionApplyTo;
    Date createAt;
    Date deleteAt;
}
