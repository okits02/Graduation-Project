package com.example.rating_service.services;

import com.example.rating_service.dto.request.ModifyRatingRequest;
import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.dto.response.RatingResponse;
import com.example.rating_service.enums.RatingFilterType;
import com.example.rating_service.model.Rating;
import com.okits02.common_lib.dto.PageResponse;

public interface RatingService {
    public RatingResponse createRating(RatingRequest request);
    public RatingResponse modifyRating(ModifyRatingRequest request);
    public PageResponse<RatingResponse> getAllByFilter(int page, int size, RatingFilterType type, String productId);
    public void deleteRating(String id );
}
