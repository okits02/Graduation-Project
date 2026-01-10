package com.example.rating_service.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingEvent {
    String productId;
    private Double avgRating;
}
