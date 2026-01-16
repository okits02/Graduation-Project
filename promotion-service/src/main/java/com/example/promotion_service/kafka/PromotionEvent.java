package com.example.promotion_service.kafka;

import com.example.promotion_service.enums.PromotionKind;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionEvent {
    String id;
    String name;
    String campaignId;
    String descriptions;
    Double discountPercent;
    PromotionKind promotionKind;
    Double fixedAmount;
    String applyTo;
    Boolean active;
    Set<String> productIdList;
    Set<String> categoryIdList;
    Date createAt;
    Date updateAt;
}
