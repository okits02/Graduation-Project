package com.example.promotion_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionApplyToResponse {
    String id;
    Set<String> productId;
    String categoryName;
}
