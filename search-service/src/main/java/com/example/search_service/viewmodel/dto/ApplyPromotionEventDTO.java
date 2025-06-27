package com.example.search_service.viewmodel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyPromotionEventDTO {
    String id;
    String name;
    String description;
    double discountPercent;
    double fixedAmount;
    boolean isActive;
    Set<String> productIdList;
    Set<String> categoryIdList;
}
