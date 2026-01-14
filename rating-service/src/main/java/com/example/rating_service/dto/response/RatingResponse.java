package com.example.rating_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    String id;
    String firstName;
    String lastName;
    String avatarUrl;
    String userId;
    String content;
    Double ratingScore;
    String productId;
    boolean isVerifiedPurchase;
    LocalDate createdAt;
}
