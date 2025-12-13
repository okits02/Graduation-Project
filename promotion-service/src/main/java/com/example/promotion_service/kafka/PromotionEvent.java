package com.example.promotion_service.kafka;

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
    String descriptions;
    Double discountPercent;
    Double fixedAmount;
    String applyTo;
    Boolean active;
    Set<String> productIdList;
    Set<String> categoryIdList;
    Date createAt;
    Date updateAt;
}
