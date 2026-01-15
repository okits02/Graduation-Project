package com.example.rating_service.controller;

import com.example.rating_service.dto.request.ModifyRatingRequest;
import com.example.rating_service.dto.request.RatingRequest;
import com.example.rating_service.dto.response.RatingResponse;
import com.example.rating_service.dto.response.RatingSummaryResponse;
import com.example.rating_service.enums.RatingFilterType;
import com.example.rating_service.model.Rating;
import com.example.rating_service.services.RatingService;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rating")
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/create")
    public ApiResponse<RatingResponse> save(
            @RequestBody RatingRequest request
    ){
        return ApiResponse.<RatingResponse>builder()
                .code(200)
                .message("creation rating successfully!")
                .result(ratingService.createRating(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<RatingResponse> update(
            @RequestBody ModifyRatingRequest request
    ){
        return ApiResponse.<RatingResponse>builder()
                .code(200)
                .message("creation rating successfully!")
                .result(ratingService.modifyRating(request))
                .build();
    }

    @PutMapping("/image")
    public ApiResponse<?> uploadImageComment(
            @RequestParam("imageUrl") List<String> imageUrl,
            @RequestParam(value = "id") String id){
        ratingService.createImage(imageUrl, id);
        return ApiResponse.builder()
                .code(200)
                .build();
    }

    @GetMapping("/get/filter")
    public ApiResponse<PageResponse<RatingResponse>> getAllByFilter(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "RatingFilterType", defaultValue = "ALL") RatingFilterType type,
            @RequestParam(value = "productId") String productId
            ){
        return ApiResponse.<PageResponse<RatingResponse>>builder()
                .code(200)
                .message("get all rating successfully!")
                .result(ratingService.getAllByFilter(page - 1, size, type, productId))
                .build();
    }

    @GetMapping("/get/summary")
    public ApiResponse<RatingSummaryResponse> getRatingSummary(
            @RequestParam(value = "productId") String productId
    ){
        return ApiResponse.<RatingSummaryResponse>builder()
                .code(200)
                .message("get rating summary by product successfully!")
                .result(ratingService.getRatingSummary(productId))
                .build();
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(
            @RequestParam(value = "id") String id
    ){
        ratingService.deleteRating(id);
        return ApiResponse.builder()
                .code(200)
                .message("deleteRating successfully")
                .build();
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<RatingResponse>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<RatingResponse>>builder()
                .code(200)
                .message("get all rating successfully")
                .result(ratingService.getAll(page-1, size))
                .build();
    }
}
