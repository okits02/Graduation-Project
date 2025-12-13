package com.example.promotion_service.services.Impl;

import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.okits02.common_lib.dto.PageResponse;
import com.example.promotion_service.dto.response.PromotionResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.promotion_service.exception.PromotionErrorCode;
import com.example.promotion_service.mapper.PromotionMapper;
import com.example.promotion_service.model.Promotion;
import com.example.promotion_service.model.PromotionApplyTo;
import com.example.promotion_service.repository.PromotionApplyToRepository;
import com.example.promotion_service.repository.PromotionRepository;
import com.example.promotion_service.services.PromotionService;
import com.example.promotion_service.utils.VoucherCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static com.example.promotion_service.enums.UsageType.LIMITED;

@Slf4j
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
            throw new AppException(PromotionErrorCode.PROMOTION_EXISTS);
        }
        Promotion promotion = promotionRepository.save(promotionMapper.toPromotion(request));
        List<PromotionApplyTo> promotionApplyTo = new ArrayList<>();
        switch (request.getApplyTo())
        {
            case Product -> {
                if (request.getProductId() != null && !request.getProductId().isEmpty()) {
                    for(String id : request.getProductId()){
                        promotionApplyTo.add(PromotionApplyTo.builder()
                                        .promotion(promotion)
                                        .productId(id)
                                .build());
                    }
                }else{
                    throw new AppException(PromotionErrorCode.INVALID_PRODUCT_IDS);
                }
            }

            case Category -> {
                if(request.getCategoryId() != null && !request.getCategoryId().isEmpty()){
                    for(String id : request.getCategoryId()){
                        promotionApplyTo.add(PromotionApplyTo.builder()
                                        .promotion(promotion)
                                        .categoryId(id)
                                .build());
                    }
                }else{
                    throw new AppException(PromotionErrorCode.INVALID_CATEGORY_IDS);
                }
            }
        }
        if(request.isVoucher() && (request.getUsageType().equals(LIMITED)))
        {
            String voucherCode = VoucherCodeUtils.generateVoucherCode();
            promotion.setVoucherCode(voucherCode);
        }
        applyToRepository.saveAll(promotionApplyTo);
        promotion.setPromotionApplyTo(promotionApplyTo);
        promotion.setCreateAt(Date.from(Instant.now()));
        promotionRepository.save(promotion);
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PromotionResponse updatePromotion(PromotionUpdateRequest request) {
        Optional<Promotion> promotion = promotionRepository.findById(request.getId());
        if(promotion.isEmpty()){
            throw new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS);
        }
        List<PromotionApplyTo> promotionApplyTos = promotion.get().getPromotionApplyTo();
        if(request.getDeleteApplyTo() != null){
            switch (request.getApplyTo()){
                case Product -> {
                    for(String id : request.getDeleteApplyTo()){
                        PromotionApplyTo promotionApplyTo =applyToRepository.findByProductIdAndPromotion(id,
                                promotion.get().getId());
                        promotionApplyTos.remove(promotionApplyTo);
                    }
                }
                case Category -> {
                    for(String name : request.getDeleteApplyTo()){
                        PromotionApplyTo promotionApplyTo =applyToRepository.findByCategoryNameAndPromotion(name,
                                promotion.get().getId());
                        promotionApplyTos.remove(promotionApplyTo);
                    }
                }
            }
        }
        switch (request.getApplyTo()){
            case Product -> {
                if (request.getProductId() != null && !request.getProductId().isEmpty()) {
                    for(String id : request.getCategoryId()){
                        boolean exists = applyToRepository
                                .existsByPromotionAndProductId(promotion.orElse(null), id);

                        if (!exists) {
                            promotionApplyTos.add(
                                    PromotionApplyTo.builder()
                                            .promotion(promotion.get())
                                            .productId(id)
                                            .build()
                            );
                        }
                    }
                }else{
                    throw new AppException(PromotionErrorCode.INVALID_PRODUCT_IDS);
                }
            }

            case Category -> {
                if(request.getCategoryId() != null && !request.getCategoryId().isEmpty()){
                    for(String id : request.getCategoryId()){
                        boolean exists = applyToRepository
                                .existsByPromotionAndCategoryId(promotion.orElse(null), id);
                        if (!exists) {
                            promotionApplyTos.add(
                                    PromotionApplyTo.builder()
                                            .promotion(promotion.get())
                                            .categoryId(id)
                                            .build()
                            );
                        }
                    }
                }else{
                    throw new AppException(PromotionErrorCode.INVALID_CATEGORY_IDS);
                }
            }
        }
        promotionMapper.updatePromotion(promotion.orElse(null), request);
        promotion.get().setPromotionApplyTo(promotionApplyTos);
        promotionRepository.save(promotion.get());
        return promotionMapper.toPromotionResponse(promotion.orElse(null));
    }

    @Override
    public PromotionResponse getPromotion(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() ->
                new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS));
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PageResponse<PromotionResponse> getAllPromotion(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = promotionRepository.findAll(pageable);
        return PageResponse.<PromotionResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(promotionMapper::toPromotionResponse).toList())
                .build();
    }

    @Override
    public void deletePromotion(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() ->
                new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS));
        promotionRepository.deleteById(promotionId);
    }
}
