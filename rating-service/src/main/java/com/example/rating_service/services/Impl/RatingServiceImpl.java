package com.example.rating_service.services.Impl;

import com.example.rating_service.dto.CustomerVM;
import com.example.rating_service.dto.RatingEvent;
import com.example.rating_service.dto.request.ModifyRatingRequest;
import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.dto.response.RatingResponse;
import com.example.rating_service.dto.response.RatingSummaryResponse;
import com.example.rating_service.dto.response.UserIdResponse;
import com.example.rating_service.enums.RatingFilterType;
import com.example.rating_service.helper.RatingMapperHelper;
import com.example.rating_service.repository.httpClient.OrderClient;
import com.example.rating_service.repository.httpClient.UserClient;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.rating_service.exception.RatingErrorCode;
import com.example.rating_service.mapper.RatingMapper;
import com.example.rating_service.model.Rating;
import com.example.rating_service.repository.RatingRepository;
import com.example.rating_service.repository.httpClient.ProfileClient;
import com.example.rating_service.services.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class RatingServiceImpl implements RatingService {
    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;
    private final RatingMapperHelper ratingMapperHelper;
    private final ProfileClient profileClient;
    private final UserClient userClient;
    private final OrderClient orderClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    public RatingResponse createRating(RatingRequest request) {
        UserIdResponse user = getUserId();
        if ("ADMIN".equals(user.getRole())) {
            throw new AppException(RatingErrorCode.ADMIN_CANNOT_RATE);
        }

        if (ratingRepository.existsByUserIdAndProductId(user.getUserId(), request.getProductId())) {
            throw new AppException(RatingErrorCode.RATING_EXISTS);
        }

        var response = profileClient.getProfileForRating(user.getUserId()).getBody();
        if (response == null || response.getCode() != 200) {
            throw new AppException(RatingErrorCode.PROFILE_NOT_EXISTS);
        }

        Rating rating = ratingMapper.toRating(request);
        rating.setUserId(user.getUserId());

        log.info("[RATING][CREATE] userId={}, productId={}", user.getUserId(), request.getProductId());

        var responseOrder = orderClient.checkVerifiedPurchase(user.getUserId(), request.getProductId());
        rating.setVerifiedPurchase(responseOrder.getResult().getIsVerifiedPurchase());

        var saved = ratingRepository.save(rating);
        var ratingResponse = ratingMapper.toRatingResponse(saved);

        ratingResponse.setAvatarUrl(response.getResult().getAvatarUrl());
        ratingResponse.setFirstName(response.getResult().getFirstName());
        ratingResponse.setLastName(response.getResult().getLastName());
        ratingResponse.setCreatedAt(LocalDateTime.now());

        publishRatingEvent(ratingResponse.getProductId());
        return ratingResponse;
    }

    @Override
    public RatingResponse modifyRating(ModifyRatingRequest request) {
        Rating rating = ratingRepository.findById(request.getId()).orElseThrow(() ->
                new AppException(RatingErrorCode.RATING_EXISTS));
        ratingMapper.updateRating(rating, request);
        return ratingMapper.toRatingResponse(ratingRepository.save(rating));
    }

    @Override
    public PageResponse<RatingResponse> getAllByFilter(int page, int size, RatingFilterType type, String productId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Rating> ratingPage = null;

        switch (type) {
            case HAVE_PURCHASE -> ratingPage =
                    ratingRepository.findAllByProductIdAndIsVerifiedPurchaseTrue(
                            productId, pageable
                    );

            case STAR_FIVE -> ratingPage =
                    ratingRepository.findAllByProductIdAndRatingScore(
                            productId, 5.0, pageable
                    );

            case STAR_FOUR -> ratingPage =
                    ratingRepository.findAllByProductIdAndRatingScore(
                            productId, 4.0, pageable
                    );

            case STAR_THREE -> ratingPage =
                    ratingRepository.findAllByProductIdAndRatingScore(
                            productId, 3.0, pageable
                    );

            case STAR_TWO -> ratingPage =
                    ratingRepository.findAllByProductIdAndRatingScore(
                            productId, 2.0, pageable
                    );

            case STAR_ONE -> ratingPage =
                    ratingRepository.findAllByProductIdAndRatingScore(
                            productId, 1.0, pageable
                    );

            case ALL -> ratingPage =
                    ratingRepository.findAllByProductId(
                            productId, pageable
                    );
        }
        List<RatingResponse> data = ratingPage.getContent()
                .stream()
                .map(rating -> ratingMapperHelper.toResponse(rating, rating.getUserId()))
                .toList();

        return PageResponse.<RatingResponse>builder()
                .data(data)
                .currentPage(page)
                .totalElements(ratingPage.getTotalElements())
                .totalPage(ratingPage.getTotalPages())
                .build();
    }

    public RatingSummaryResponse getRatingSummary(String productId) {

        var result = ratingRepository.getRatingSummary(productId);

        BigDecimal avgRating = result.getAverage();
        Long totalReviews = result.getTotal();
        return RatingSummaryResponse.builder()
                .averageRating(avgRating.doubleValue())
                .totalReviews(Math.toIntExact(totalReviews))
                .maxRating(5)
                .build();
    }

    @Override
    public void deleteRating(String id) {
        Rating rating = ratingRepository.findById(id).orElseThrow(() ->
                new AppException(RatingErrorCode.RATING_EXISTS));
        ratingRepository.delete(rating);
        publishRatingEvent(rating.getProductId());
    }
    private UserIdResponse getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult();
    }

    private void publishRatingEvent(String productId){
        Double avgRating = ratingRepository
                .calculateAvgRatingByProductId(productId);

        if (avgRating == null) {
            avgRating = 0.0;
        }
        avgRating = Math.min(avgRating, 5.0);
        avgRating = Math.max(avgRating, 0.0);

        RatingEvent event = RatingEvent.builder()
                .productId(productId)
                .avgRating(avgRating)
                .build();
        sendRatingEvent(event);
    }


    private void sendRatingEvent(RatingEvent ratingEvent){
        kafkaTemplate.send("rating-event", ratingEvent).whenComplete(
                (result, ex) -> {
                    if(ex != null)
                    {
                        System.err.println("Failed to send message" + ex.getMessage());
                    }else
                    {
                        System.err.println("send message successfully" + result.getProducerRecord());
                    }
                });
    }

}
