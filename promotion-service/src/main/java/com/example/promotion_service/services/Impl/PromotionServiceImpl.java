package com.example.promotion_service.services.Impl;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.dto.response.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.example.promotion_service.enums.UsageType;
import com.example.promotion_service.exception.AppException;
import com.example.promotion_service.exception.ErrorCode;
import com.example.promotion_service.mapper.PromotionMapper;
import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.model.PromotionApplyTo;
import com.example.promotion_service.repository.PromotionApplyToRepository;
import com.example.promotion_service.repository.PromotionRepository;
import com.example.promotion_service.services.PromotionService;
import com.example.promotion_service.utils.VoucherCodeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static com.example.promotion_service.enums.ApplyTo.Category;
import static com.example.promotion_service.enums.ApplyTo.Product;
import static com.example.promotion_service.enums.UsageType.LIMITED;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionApplyToRepository applyToRepository;
    private final PromotionMapper promotionMapper;

    @Override
    public PromotionResponse createPromotion(PromotionCreationRequest request) {
        if(promotionRepository.existsByName(request.getName()))
        {
            throw new AppException(ErrorCode.PROMOTION_EXISTS);
        }
        Promotion promotion = promotionRepository.save(promotionMapper.toPromotion(request));
        PromotionApplyTo promotionApplyTo = new PromotionApplyTo();
        switch (request.getApplyTo())
        {
            case Product -> {
                if (request.getProductId() != null && !request.getProductId().isEmpty()) {
                    promotionApplyTo.setPromotion(promotion);
                    promotionApplyTo.setProductId(new HashSet<>(request.getProductId()));
                }else{
                    throw new AppException(ErrorCode.INVALID_PRODUCT_IDS);
                }
            }

            case Category -> {
                if(request.getCategoryName() != null && !request.getCategoryName().isEmpty()){
                    promotionApplyTo.setPromotion(promotion);
                    promotionApplyTo.setCategoryName(new HashSet<>(request.getCategoryName()));
                }else{
                    throw new AppException(ErrorCode.INVALID_CATEGORY_IDS);
                }
            }
        }
        if(request.isVoucher() && (request.getUsageType().equals(LIMITED)))
        {
            String voucherCode = VoucherCodeUtils.generateVoucherCode();
            promotion.setVoucherCode(voucherCode);
        }
        applyToRepository.save(promotionApplyTo);
        promotion.setPromotionApplyTo(promotionApplyTo);
        promotion.setCreateAt(Date.from(Instant.now()));
        promotionRepository.save(promotion);
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PromotionResponse updatePromotion(PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findByName(request.getName());
        PromotionApplyTo promotionApplyTo = applyToRepository.findByPromotionId(promotion.getId());
        if(!promotion.getApplyTo().equals(request.getApplyTo())) {
            switch (request.getApplyTo()){
                case Product -> {
                    promotionApplyTo.setProductId(new HashSet<>(request.getProductId()));
                    promotionApplyTo.setCategoryName(null);
                }
                case Category -> {
                    promotionApplyTo.setCategoryName(new HashSet<>(request.getCategoryId()));
                    promotionApplyTo.setProductId(null);
                }
            }
        } else {
            switch (request.getApplyTo()){
                case Product -> {
                    Set<String> productId = promotionApplyTo.getProductId();
                    if(request.getDeleteApplyTo() != null) {
                        productId.removeAll(request.getProductId());
                    }
                    if(request.getProductId() != null) {
                        productId.addAll(request.getProductId());
                    }
                    promotionApplyTo.setProductId(productId);
                }
                case Category -> {
                    Set<String> categoryId = promotionApplyTo.getCategoryName();
                    if(request.getDeleteApplyTo() != null)
                    {
                        categoryId.removeAll(request.getCategoryId());
                    }
                    if (request.getCategoryId() != null) {
                        categoryId.addAll(request.getCategoryId());
                    }
                    promotionApplyTo.setCategoryName(categoryId);
                }
            }
        }
        promotion.setPromotionApplyTo(promotionApplyTo);
        promotionMapper.updatePromotion(promotion, request);
        return  promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    @Override
    public PromotionResponse getPromotion(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() ->
                new AppException(ErrorCode.PROMOTION_NOT_EXISTS));
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PageResponse<PromotionResponse> getAllPromotion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = promotionRepository.findAll(pageable);
        return PageResponse.<PromotionResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElement(pageData.getTotalElements())
                .Data(pageData.getContent().stream().map(promotionMapper::toPromotionResponse).toList())
                .build();
    }

    @Override
    public void deletePromotion(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() ->
                new AppException(ErrorCode.PROMOTION_NOT_EXISTS));
        applyToRepository.deleteById(promotion.getPromotionApplyTo().getId());
        promotionRepository.deleteById(promotionId);
    }
}
