package com.example.promotion_service.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

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
    boolean active;
    Set<String> productIdList;
    Set<String> categoryNameList;
}
