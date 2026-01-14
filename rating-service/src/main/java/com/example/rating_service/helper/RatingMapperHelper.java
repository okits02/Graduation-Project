package com.example.rating_service.helper;

import com.example.rating_service.dto.response.RatingResponse;
import com.example.rating_service.exception.RatingErrorCode;
import com.example.rating_service.mapper.RatingMapper;
import com.example.rating_service.model.Rating;
import com.example.rating_service.repository.httpClient.ProfileClient;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingMapperHelper {
    private final ProfileClient profileClient;
    private final RatingMapper ratingMapper;
    public RatingResponse toResponse(Rating rating, String userId){
        var response = profileClient.getProfileForRating(userId);
        if(response == null || response.getBody().getCode() != 200)
        {
            throw new AppException(RatingErrorCode.PROFILE_NOT_EXISTS);
        }
        RatingResponse ratingResponse = ratingMapper.toRatingResponse(rating);
        ratingResponse.setLastName(response.getBody().getResult().getLastName());
        ratingResponse.setFirstName(response.getBody().getResult().getFirstName());
        ratingResponse.setAvatarUrl(response.getBody().getResult().getAvatarUrl());
        ratingResponse.setImageUrl(rating.getImageUrl());
        ratingResponse.setCreatedAt(rating.getCreatedAt());
        return ratingResponse;
    }
}
