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

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Data
@Builder
@Setter
@Getter
public class PromotionResponse {
    String id;
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
    int usageLimited;
    int usageLimitPerUser;
    Double minimumOrderPurchaseAmount;
    Double maxDiscountAmount;
    Boolean active;
    List<String> productId;
    List<String> categoryId;
    LocalDate createAt;
    LocalDate updateAt;
}
