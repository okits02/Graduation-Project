package com.example.promotion_service.controller;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.dto.response.ApiResponse;
import com.example.promotion_service.dto.response.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.example.promotion_service.exception.AppException;
import com.example.promotion_service.exception.ErrorCode;
import com.example.promotion_service.kafka.PromotionEvent;
import com.example.promotion_service.kafka.StatusEvent;
import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.services.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import static com.example.promotion_service.enums.UsageType.LIMITED;

@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {
    private final PromotionService promotionService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping("/create")
    ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(@RequestBody PromotionCreationRequest request)
    {
        if (request.getUsageLimited() == 0)
            if (request.getUsageType().equals(LIMITED)) {
                throw new AppException(ErrorCode.USAGE_LIMITED_NULL);
            }
        PromotionResponse promotion = promotionService.createPromotion(request);
        PromotionEvent promotionEvent = PromotionEvent.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                 .descriptions(promotion.getDescriptions())
                .active(promotion.getActive())
                .discountPercent(promotion.getDiscountPercent())
                .fixedAmount(promotion.getFixedAmount())
                .productIdList(promotion.getPromotionApplyTo().getProductId())
                .categoryNameList(promotion.getPromotionApplyTo().getCategoryName())
                .build();
        kafkaTemplate.send("promotion-event", promotionEvent).whenComplete(
                (result, ex) -> {
            if (ex != null)
            {
                System.err.println("Failed to send message" + ex.getMessage());
            } else {
                System.err.println("send message successfully" + result.getProducerRecord());
            }
        });

        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Create promotion successfully!")
                        .Result(promotion)
                        .build());
    }

    @PutMapping("/update")
    ResponseEntity<ApiResponse<PromotionResponse>> updatePromotion(@RequestBody PromotionUpdateRequest request) {
        if(request.getUsageLimited() == 0)
        {
            if(request.getUsageType().equals(LIMITED))
            {
                throw new AppException(ErrorCode.USAGE_LIMITED_NULL);
            }
        }
        PromotionResponse promotionResponse = promotionService.updatePromotion(request);
        PromotionEvent promotionEvent = PromotionEvent.builder()
                .id(promotionResponse.getId())
                .name(promotionResponse.getName())
                .descriptions(promotionResponse.getDescriptions())
                .active(promotionResponse.getActive())
                .discountPercent(promotionResponse.getDiscountPercent())
                .fixedAmount(promotionResponse.getFixedAmount())
                .productIdList(promotionResponse.getPromotionApplyTo().getProductId())
                .categoryNameList(promotionResponse.getPromotionApplyTo().getCategoryName())
                .build();
        kafkaTemplate.send("promotion-event", promotionEvent).whenComplete(
                (result, ex) -> {
            if(ex != null)
            {
                System.err.println("Failed to send message" + ex.getMessage());
            }else {
                System.err.println("send message successfully" + result.getProducerRecord());
            }
        });

        return  ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Update promotion successfully!")
                        .Result(promotionResponse)
                        .build());
    }

    @GetMapping("/getPromotion/{promotionId}")
    ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable String promotionId){
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Get promotion successfully!")
                        .Result(promotionService.getPromotion(promotionId))
                        .build());
    }

    @GetMapping("/getAll")
    ResponseEntity<ApiResponse<PageResponse<PromotionResponse>>> getAllPromotion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(ApiResponse.<PageResponse<PromotionResponse>>builder()
                        .code(200)
                        .message("Get all promotion successfully!")
                        .Result(promotionService.getAllPromotion(page, size))
                .build());
    }

    @DeleteMapping("/delete/{promotionId}")
    ResponseEntity<ApiResponse<?>> deletePromotion(@PathVariable String promotionId){
        try {
            promotionService.deletePromotion(promotionId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.builder()
                    .code(204)
                    .message("User deleted successfully")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    public void UpdatePromotionStatus(String id){
        StatusEvent statusEvent = StatusEvent.builder()
                .id(id)
                .build();
        kafkaTemplate.send("promotion-status-event", statusEvent).whenComplete(
                (result, ex) -> {
                    if (ex != null)
                    {
                        System.err.println("Failed to send message" + ex.getMessage());
                    } else {
                        System.err.println("send message successfully" + result.getProducerRecord());
                    }
                });
    }
}
