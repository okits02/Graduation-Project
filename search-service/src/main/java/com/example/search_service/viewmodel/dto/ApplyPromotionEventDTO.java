package com.example.search_service.viewmodel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyPromotionEventDTO {
    String id;
    String name;
    String descriptions;
    BigDecimal discountPercent;
    BigDecimal fixedAmount;
    Boolean active;
    Set<String> productIdList;
    Set<String> categoryNameList;
}
