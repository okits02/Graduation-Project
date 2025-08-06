package com.example.rating_service.services.Impl;

import com.example.rating_service.dto.CustomerVM;
import com.example.rating_service.dto.request.ModifyRatingRequest;
import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.dto.response.RatingResponse;
import com.example.rating_service.exception.AppException;
import com.example.rating_service.exception.ErrorCode;
import com.example.rating_service.mapper.RatingMapper;
import com.example.rating_service.model.Rating;
import com.example.rating_service.repository.RatingRepository;
import com.example.rating_service.repository.httpClient.ProfileClient;
import com.example.rating_service.services.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final ProfileClient profileClient;


    @Override
    public RatingResponse createRating(RatingRequest request) {
        if(ratingRepository.existsByCreatedByAndProductId(request.getCreateBy(), request.getProductId())){
            throw new AppException(ErrorCode.RATING_EXISTS);
        };
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var response = profileClient.getProfileForRating(authHeader).getBody();
        if (response == null || response.getCode() != 200) {
            throw new RuntimeException("Failed to delete profile from Profile-service");
        }
        Rating rating = ratingMapper.toRating(request);
        CustomerVM customerVM = response.getResult();
        rating.setFirstName(customerVM.getFirstName());
        rating.setLastName(customerVM.getLastName());
    }

    @Override
    public RatingResponse modifyRating(ModifyRatingRequest request) {
        return null;
    }

    @Override
    public void deleteRating(String id, String userName, String productId) {

    }
}
