package com.example.promotion_service.controller;

import com.example.promotion_service.dto.request.CheckValidVoucherRequest;
import com.example.promotion_service.dto.request.FlashSaleCreationRequest;
import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.dto.response.PromotionEndingSoonResponse;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.promotion_service.exception.PromotionErrorCode;
import com.example.promotion_service.services.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.hibernate.query.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import static com.example.promotion_service.enums.UsageType.LIMITED;

@RestController
@RequestMapping("/promotion")
@RequiredArgsConstructor
@Slf4j
public class PromotionController {
    private final PromotionService promotionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
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

    @PostMapping("/flashSale")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<PromotionResponse>> createFlashSale(@RequestBody FlashSaleCreationRequest request){
        return ApiResponse.<List<PromotionResponse>>builder()
                .code(200)
                .message("creation promotion flashSale successfully")
                .result(promotionService.createPromotionFlashSale(request))
                .build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<PromotionResponse>> getPromotionById(@PathVariable String promotionId){
        return ResponseEntity.ok(ApiResponse.<PromotionResponse>builder()
                        .code(200)
                        .message("Get promotion successfully!")
                        .result(promotionService.getPromotion(promotionId))
                        .build());
    }

    @GetMapping("/auto/getAll")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/getAll")
    ApiResponse<PageResponse<PromotionResponse>> getAllPromotion(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<PromotionResponse>>builder()
                .code(200)
                .message("get all promotion auto and voucher successfully!")
                .result(promotionService.getAllPromotion(page - 1, size))
                .build();
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

    @GetMapping("/flash-sale/getAll")
    ApiResponse<PageResponse<PromotionResponse>> getAllPromotionFlashSalle(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<PromotionResponse>>builder()
                .code(200)
                .message("Get all voucher successfully")
                .result(promotionService.getPromotionFlashSale(page - 1, size))
                .build();
    }

    @DeleteMapping("/delete/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/voucher")
    ApiResponse<List<PromotionResponse>> getListVoucherForOrder(
            @RequestParam("skus") List<String> skus,
            @RequestParam("totalAmount") Double totalAmount,
            @RequestParam("today") LocalDate today
    ){
        return ApiResponse.<List<PromotionResponse>>builder()
                .code(200)
                .message("get list voucher successfully!")
                .result(promotionService.getPromotionForOrder(skus, totalAmount, today))
                .build();
    }

    @PostMapping("/internal/voucher/check")
    ApiResponse<PromotionResponse> checkValidForOrder(
            @RequestBody CheckValidVoucherRequest request){
        return ApiResponse.<PromotionResponse>builder()
                .code(200)
                .message("voucher is valid")
                .result(promotionService.checkValidVoucher(request))
                .build();
    }
    @PostMapping("/internal/voucher/applyForOrder")
    ApiResponse<?> applyForOrder(
                    @RequestParam(value = "orderId") String orderId,
                    @RequestParam(value = "voucherCode") String voucherCode){
        promotionService.applyVoucherToOrder(voucherCode, orderId);
        return ApiResponse.builder()
                .code(200)
                .message("apply voucher for order is successfully")
                .build();
    }

    @PostMapping("/internal/voucher/rollBack")
    ApiResponse<?> rollBackVoucher(
            @RequestParam("orderId") String orderId
    ){
        promotionService.rollbackVoucher(orderId);
        return ApiResponse.builder()
                .code(200)
                .message("Roll back voucher successfully")
                .build();
    }

    @GetMapping("/internal/flashSale")
    ApiResponse<PromotionEndingSoonResponse> getListPromotionIdsEndingSoon(){
        return ApiResponse.<PromotionEndingSoonResponse>builder()
                .code(200)
                .message("get list promotion flash salle successfully!")
                .result(promotionService.getListPromotionEndingSoon())
                .build();
    }
}
