package com.example.rating_service.mapper;

import com.example.rating_service.dto.request.ModifyRatingRequest;
import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.dto.response.RatingResponse;
import com.example.rating_service.model.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    Rating toRating(RatingRequest request);
    RatingResponse toRatingResponse(Rating rating);
    void updateRating(@MappingTarget Rating rating, ModifyRatingRequest request);
}
