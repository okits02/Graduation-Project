package com.example.search_service.viewmodel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePromotionDTO {
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
    Date createAt;
    Date updateAt;
}
