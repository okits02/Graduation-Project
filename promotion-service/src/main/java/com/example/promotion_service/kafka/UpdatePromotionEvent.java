package com.example.promotion_service.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePromotionEvent {
    String id;
    String name;
    String descriptions;
    Double discountPercent;
    Double fixedAmount;
    String applyTo;
    Boolean active;
    Set<String> productIdList;
    Set<String> categoryIdList;
    List<String> deleteApplyTo;
    LocalDate createAt;
    LocalDate updateAt;
}
