package com.example.promotion_service.controller;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.kafka.UpdatePromotionEvent;
import com.example.promotion_service.model.Promotion;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.promotion_service.exception.PromotionErrorCode;
import com.example.promotion_service.kafka.PromotionEvent;
import com.example.promotion_service.kafka.StatusEvent;
import com.example.promotion_service.services.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.example.promotion_service.enums.UsageType.LIMITED;

@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {
    private final PromotionService promotionService;

    @PostMapping("/create")
    ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@RequestBody PromotionCreationRequest request)
    {
        if (request.getUsageLimited() == 0)
            if (request.getUsageType().equals(LIMITED)) {
                throw new AppException(PromotionErrorCode.USAGE_LIMITED_NULL);
            }
        PromotionResponse promotion = promotionService.createPromotion(request);
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Create promotion successfully!")
                        .result(promotion)
                        .build());
    }

    @PutMapping("/update")
    ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(@RequestBody PromotionUpdateRequest request) {
        if(request.getUsageLimited() == 0)
        {
            if(request.getUsageType().equals(LIMITED))
            {
                throw new AppException(PromotionErrorCode.USAGE_LIMITED_NULL);
            }
        }
        PromotionResponse promotionResponse = promotionService.updatePromotion(request);

        return  ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Update promotion successfully!")
                        .result(promotionResponse)
                        .build());
    }

    @GetMapping("/getPromotion/{promotionId}")
    ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable String promotionId){
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Get promotion successfully!")
                        .result(promotionService.getPromotion(promotionId))
                        .build());
    }

    @GetMapping("/getAll")
    ResponseEntity<ApiResponse<PageResponse<PromotionResponse>>> getAllPromotionAuto(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(ApiResponse.<PageResponse<PromotionResponse>>builder()
                        .code(200)
                        .message("Get all promotion successfully!")
                        .result(promotionService.getAllPromotionAuto(page - 1, size))
                .build());
    }

    @GetMapping("/voucher/getAll")
    ApiResponse<PageResponse<PromotionResponse>> getAllPromotionVoucher(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<PromotionResponse>>builder()
                .code(200)
                .message("Get all voucher successfully")
                .result(promotionService.getPromotionVoucher(page - 1, size))
                .build();
    }

    @DeleteMapping("/delete/{promotionId}")
    ResponseEntity<ApiResponse<?>> deletePromotion(@PathVariable String promotionId){
        try {
            promotionService.deletePromotion(promotionId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.builder()
                    .code(200)
                    .message("User deleted successfully")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/internal/get-promotion-by-cate")
    ApiResponse<List<PromotionResponse>> getByCategoryIds(@RequestParam List<String> categoryIds){
        return ApiResponse.<List<PromotionResponse>>builder()
                .code(200)
                .message("get list promotion successfully!")
                .result(promotionService.getPromotionByCategoryIds(categoryIds))
                .build();
    }
}
