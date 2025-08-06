package com.example.rating_service.services;

import com.example.rating_service.dto.request.ModifyRatingRequest;
import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.dto.response.RatingResponse;

public interface RatingService {
    public RatingResponse createRating(RatingRequest request);
    public RatingResponse modifyRating(ModifyRatingRequest request);
    public void deleteRating(String id, String userName, String productId);
}
