package com.example.rating_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    String id;
    String content;
    Double ratingScore;
    String productId;
    String userId;
    boolean isVerifiedPurchase;
}
