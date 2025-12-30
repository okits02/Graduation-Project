package com.example.promotion_service.services.Impl;

import com.example.promotion_service.dto.request.CategoryLevelValidateRequest;
import com.example.promotion_service.dto.request.PromotionCreationRequest;
import com.example.promotion_service.dto.request.PromotionUpdateRequest;
import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.PromotionKind;
import com.example.promotion_service.kafka.PromotionEvent;
import com.example.promotion_service.kafka.StatusEvent;
import com.example.promotion_service.kafka.UpdatePromotionEvent;
import com.example.promotion_service.repository.httpClient.ProductClient;
import com.okits02.common_lib.dto.ApiResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.promotion_service.enums.UsageType.LIMITED;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionApplyToRepository applyToRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PromotionMapper promotionMapper;
    private final ProductClient productClient;

    @Override
    public PromotionResponse createPromotion(PromotionCreationRequest request) {
        if(promotionRepository.existsByName(request.getName()))
        {
            throw new AppException(PromotionErrorCode.PROMOTION_EXISTS);
        }
        Promotion promotion = promotionMapper.toPromotion(request);
        if(request.getPromotionKind().equals(PromotionKind.AUTO)) {
            List<PromotionApplyTo> promotionApplyTo = new ArrayList<>();
            switch (request.getApplyTo()) {
                case Product -> {
                    if (request.getProductId() != null && !request.getProductId().isEmpty()) {
                        for (String id : request.getProductId()) {
                            promotionApplyTo.add(PromotionApplyTo.builder()
                                    .promotion(promotion)
                                    .productId(id)
                                    .build());
                        }
                    } else {
                        throw new AppException(PromotionErrorCode.INVALID_PRODUCT_IDS);
                    }
                }

                case Category -> {
                    if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
                        if (!checkValidLevel(request.getCategoryId())) {
                            throw new AppException(PromotionErrorCode.INVALID_LEVEL_CATEGORY);
                        }
                        for (String id : request.getCategoryId()) {
                            promotionApplyTo.add(PromotionApplyTo.builder()
                                    .promotion(promotion)
                                    .categoryId(id)
                                    .build());
                        }
                    } else {
                        throw new AppException(PromotionErrorCode.INVALID_CATEGORY_IDS);
                    }
                }
            }
            promotion.setPromotionApplyTo(promotionApplyTo);
            promotion.setCreateAt(LocalDate.now());
            sendKafKaEvent(promotion, "CREATED", new ArrayList<>());
        }else if(request.getPromotionKind().equals(PromotionKind.VOUCHER)){
            String voucherCode = VoucherCodeUtils.generateVoucherCode();
            while (promotionRepository.existsByVoucherCode(voucherCode)){
                voucherCode = VoucherCodeUtils.generateVoucherCode();
            }
            promotion.setVoucherCode(voucherCode);
            List<PromotionApplyTo> promotionApplyTo = new ArrayList<>();
            switch (request.getApplyTo()) {
                case Product -> {
                    if (request.getProductId() != null && !request.getProductId().isEmpty()) {
                        for (String id : request.getProductId()) {
                            promotionApplyTo.add(PromotionApplyTo.builder()
                                    .promotion(promotion)
                                    .productId(id)
                                    .build());
                        }
                    } else {
                        throw new AppException(PromotionErrorCode.INVALID_PRODUCT_IDS);
                    }
                }

                case Category -> {
                    if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
                        if (!checkValidLevel(request.getCategoryId())) {
                            throw new AppException(PromotionErrorCode.INVALID_LEVEL_CATEGORY);
                        }
                        for (String id : request.getCategoryId()) {
                            promotionApplyTo.add(PromotionApplyTo.builder()
                                    .promotion(promotion)
                                    .categoryId(id)
                                    .build());
                        }
                    } else {
                        throw new AppException(PromotionErrorCode.INVALID_CATEGORY_IDS);
                    }
                }
            }
        }
        promotionRepository.save(promotion);
        return promotionMapper.toPromotionResponse(promotion);
    }

    @Override
    public PromotionResponse updatePromotion(PromotionUpdateRequest request) {
        Optional<Promotion> promotion = promotionRepository.findById(request.getId());
        if(promotion.isEmpty()){
            throw new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS);
        }
        List<PromotionApplyTo> promotionApplyTos =
                new ArrayList<>(promotion.get().getPromotionApplyTo());
        if(request.getDeleteApplyTo() != null && !request.getDeleteApplyTo().isEmpty()){
            switch (request.getApplyTo()){
                case Product -> {
                    for(String id : request.getDeleteApplyTo()){
                        PromotionApplyTo promotionApplyTo =applyToRepository.findByProductIdAndPromotion(id,
                                promotion.get().getId());
                        if (promotionApplyTo != null) {
                            promotionApplyTos.remove(promotionApplyTo);
                        }
                    }
                }
                case Category -> {
                    for(String id : request.getDeleteApplyTo()){
                        PromotionApplyTo promotionApplyTo =applyToRepository.findByCategoryNameAndPromotion(id,
                                promotion.get().getId());
                        if (promotionApplyTo != null) {
                            promotionApplyTos.remove(promotionApplyTo);
                        }
                    }
                }
            }
        }
        switch (request.getApplyTo()){
            case Product -> {
                if (request.getProductId() != null && !request.getProductId().isEmpty()) {
                    for(String id : request.getProductId()){
                        Long exists = applyToRepository
                                .existsByPromotionAndProductId(promotion.get().getId(), id);
                        if (exists == 0) {
                            promotionApplyTos.add(
                                    PromotionApplyTo.builder()
                                            .promotion(promotion.get())
                                            .productId(id)
                                            .build()
                            );
                        }
                    }
                }
            }
            case Category -> {
                if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {

                    List<String> listCate = new ArrayList<>(
                            promotionApplyTos.stream()
                                    .map(PromotionApplyTo::getCategoryId)
                                    .filter(Objects::nonNull)
                                    .toList()
                    );
                    listCate.addAll(request.getCategoryId());

                    if (!checkValidLevel(listCate)) {
                    throw new AppException(PromotionErrorCode.INVALID_LEVEL_CATEGORY);
                    }

                    for (String id : request.getCategoryId()) {
                        Long exists = applyToRepository
                                .existsByPromotionAndCategoryId(promotion.get().getId(), id);

                        if (exists == 0) {
                            promotionApplyTos.add(
                                    PromotionApplyTo.builder()
                                            .promotion(promotion.get())
                                            .categoryId(id)
                                            .build()
                            );
                        }
                    }
                }
            }
        }
        promotionMapper.updatePromotion(promotion.orElse(null), request);
        promotion.get().getPromotionApplyTo().clear();
        promotion.get().getPromotionApplyTo().addAll(promotionApplyTos);
        sendKafKaEvent(promotion.get(), "UPDATED", request.getDeleteApplyTo());
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
    public PageResponse<PromotionResponse> getPromotionVoucher(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = promotionRepository.findAllByPromotionKind(PromotionKind.VOUCHER, pageable);
        return PageResponse.<PromotionResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(promotionMapper::toPromotionResponse).toList())
                .build();
    }

    @Override
    public List<PromotionResponse> getPromotionByCategoryIds(List<String> categoryIds) {

        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        return categoryIds.stream()
                .map(applyToRepository::findByCategoryId)
                .flatMap(List::stream)
                .map(PromotionApplyTo::getPromotion)
                .filter(Objects::nonNull)
                .filter(p -> Boolean.TRUE.equals(p.getActive()))
                .map(p -> PromotionResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .descriptions(p.getDescriptions())
                        .discountPercent(p.getDiscountPercent())
                        .fixedAmount(p.getFixedAmount())
                        .applyTo(p.getApplyTo())
                        .active(p.getActive())
                        .createAt(p.getCreateAt())
                        .updateAt(p.getUpdateAt())
                        .build()
                )
                .toList();
    }

    @Override
    public PageResponse<PromotionResponse> getAllPromotionAuto(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = promotionRepository.findAllByPromotionKind(PromotionKind.AUTO, pageable);
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
        sendKafKaEvent(promotion, "DELETED", new ArrayList<>());
        promotionRepository.deleteById(promotionId);
    }

    private Boolean checkValidLevel(List<String> categoryIds){
        CategoryLevelValidateRequest request = CategoryLevelValidateRequest.builder()
                .categoryIds(categoryIds)
                .build();
        var response = productClient.CategoryValidateSameLevel(request);
        if(response.getCode() != 200)
        {
            throw new AppException(PromotionErrorCode.CAN_NOT_CONNECT_TO_PRODUCT_CLIENT);
        }
        return response.getResult().getValid();
    }

    @Override
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

    private void sendKafKaEvent(Promotion promotion, String eventType, List<String> DeleteApplyTo){
        Set<String> categoryIds = new HashSet<>();
        Set<String> productIds = new HashSet<>();
        if(promotion.getApplyTo().equals(ApplyTo.Category)){
            categoryIds = promotion.getPromotionApplyTo().stream()
                    .map(PromotionApplyTo::getCategoryId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }else if (promotion.getApplyTo().equals(ApplyTo.Product)){
            productIds = promotion.getPromotionApplyTo().stream()
                    .map(PromotionApplyTo::getProductId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        switch (eventType){
            case "CREATED" -> {
                PromotionEvent promotionEvent = PromotionEvent.builder()
                        .id(promotion.getId())
                        .name(promotion.getName())
                        .descriptions(promotion.getDescriptions())
                        .active(promotion.getActive())
                        .discountPercent(promotion.getDiscountPercent())
                        .applyTo(promotion.getApplyTo().toString())
                        .fixedAmount(promotion.getFixedAmount())
                        .productIdList(productIds)
                        .categoryIdList(categoryIds)
                        .createAt(new Date())
                        .build();
                kafkaTemplate.send("promotion-create-event", promotionEvent).whenComplete(
                        (result, ex) -> {
                            if (ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            } else {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });

            }

            case "UPDATED" -> {
                UpdatePromotionEvent updatePromotionEvent = UpdatePromotionEvent.builder()
                        .id(promotion.getId())
                        .name(promotion.getName())
                        .descriptions(promotion.getDescriptions())
                        .active(promotion.getActive())
                        .applyTo(String.valueOf(promotion.getApplyTo()))
                        .discountPercent(promotion.getDiscountPercent())
                        .fixedAmount(promotion.getFixedAmount())
                        .productIdList(productIds)
                        .categoryIdList(categoryIds)
                        .deleteApplyTo(DeleteApplyTo)
                        .createAt(promotion.getCreateAt())
                        .updateAt(LocalDate.now())
                        .build();
                kafkaTemplate.send("promotion-update-event", updatePromotionEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }

            case "DELETED" -> {
                PromotionEvent promotionEvent = PromotionEvent.builder()
                        .id(promotion.getId())
                        .build();
                kafkaTemplate.send("promotion-delete-event", promotionEvent).whenComplete(
                        (result, ex) -> {
                            if(ex != null)
                            {
                                System.err.println("Failed to send message" + ex.getMessage());
                            }else {
                                System.err.println("send message successfully" + result.getProducerRecord());
                            }
                        });
            }
        }
    }
}
