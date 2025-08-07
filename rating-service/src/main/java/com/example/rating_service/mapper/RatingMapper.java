package com.example.rating_service.mapper;

import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.model.Rating;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingMapper {
    Rating toRating(RatingRequest request);
}
