package com.example.promotion_service.services;

import com.example.promotion_service.dto.request.CheckValidVoucherRequest;
import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.model.Promotion;
import com.okits02.common_lib.dto.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;

import java.util.Date;
import java.util.List;

public interface PromotionService {
    public PromotionResponse createPromotion(PromotionCreationRequest request);
    public PromotionResponse updatePromotion(PromotionUpdateRequest request);
    public PromotionResponse getPromotion(String promotionId);
    public PageResponse<PromotionResponse> getPromotionVoucher(int page, int size);
    public List<PromotionResponse> getPromotionByCategoryIds(List<String> categoryIds);
    public PageResponse<PromotionResponse> getAllPromotionAuto(int page, int size);
    public PageResponse<PromotionResponse> getAllPromotion(int page, int size);
    public List<PromotionResponse> getPromotionForOrder(List<String> skus, Double totalAmount, Date today);
    public List<String> getListPromotionFlashSaleIds();
    public PromotionResponse checkValidVoucher(CheckValidVoucherRequest request);
    public void applyVoucherToOrder(String voucherCode, String orderId);
    public void rollbackVoucher(String orderId);
    public void UpdatePromotionStatus(String id);
    public void deletePromotion(String promotionId);
}
