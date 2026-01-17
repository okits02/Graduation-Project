package com.example.promotion_service.services.Impl;

import com.example.promotion_service.dto.ProductSkuVM;
import com.example.promotion_service.dto.request.*;
import com.example.promotion_service.dto.response.PromotionEndingSoonResponse;
import com.example.promotion_service.enums.ApplyTo;
import com.example.promotion_service.enums.DiscountType;
import com.example.promotion_service.enums.PromotionKind;
import com.example.promotion_service.kafka.PromotionEvent;
import com.example.promotion_service.kafka.StatusEvent;
import com.example.promotion_service.kafka.UpdatePromotionEvent;
import com.example.promotion_service.model.PromotionCampaign;
import com.example.promotion_service.model.PromotionUsage;
import com.example.promotion_service.repository.PromotionCampaignRepository;
import com.example.promotion_service.repository.PromotionUsageRepository;
import com.example.promotion_service.repository.httpClient.ProductClient;
import com.example.promotion_service.repository.httpClient.SearchClient;
import com.example.promotion_service.repository.httpClient.UserClient;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.promotion_service.exception.PromotionErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionCampaignRepository promotionCampaignRepository;
    private final PromotionUsageRepository promotionUsageRepository;
    private final PromotionApplyToRepository applyToRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PromotionMapper promotionMapper;
    private final ProductClient productClient;
    private final UserClient userClient;
    private final SearchClient searchClient;

    @Override
    public PromotionResponse createPromotion(PromotionCreationRequest request) {
        if(promotionRepository.existsByName(request.getName()))
        {
            throw new AppException(PromotionErrorCode.PROMOTION_EXISTS);
        }
        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new AppException(PROMOTION_CAN_NOT_CREATE);
        }
        PromotionCampaign promotionCampaign = promotionCampaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new AppException(CAMPAIGN_NOT_EXISTS));
        Promotion promotion = promotionMapper.toPromotion(request);
        promotion.setActive(false);
        promotion.setCampaign(promotionCampaign);
        if(request.getPromotionKind().equals(PromotionKind.AUTO)) {
            List<PromotionApplyTo> promotionApplyTo = new ArrayList<>();
            switch (request.getApplyTo()) {
                case ALL -> {
                    promotion.setPromotionApplyTo(Collections.emptyList());
                }
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
        }else if(request.getPromotionKind().equals(PromotionKind.VOUCHER)){
            String voucherCode = VoucherCodeUtils.generateVoucherCode();
            while (promotionRepository.existsByVoucherCode(voucherCode)){
                voucherCode = VoucherCodeUtils.generateVoucherCode();
            }
            promotion.setVoucherCode(voucherCode);
            List<PromotionApplyTo> promotionApplyTo = new ArrayList<>();
            switch (request.getApplyTo()) {
                case ALL -> {
                    promotion.setPromotionApplyTo(Collections.emptyList());
                }
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
        if (Boolean.TRUE.equals(promotion.get().getActive())) {
            throw new AppException(PROMOTION_IS_ACTIVE);
        }

        PromotionCampaign promotionCampaign = promotionCampaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new AppException(CAMPAIGN_NOT_EXISTS));
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
        promotion.get().setCampaign(promotionCampaign);
        promotion.get().getPromotionApplyTo().clear();
        promotion.get().getPromotionApplyTo().addAll(promotionApplyTos);
        promotionRepository.save(promotion.get());
        return promotionMapper.toPromotionResponse(promotion.orElse(null));
    }

    @Override
    public List<PromotionResponse> createPromotionFlashSale(FlashSaleCreationRequest request) {
        PromotionCampaign promotionCampaign = promotionCampaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new AppException(CAMPAIGN_NOT_EXISTS));
        if (request.getStartDate().isBefore(LocalDateTime.now())) {
            throw new AppException(PROMOTION_CAN_NOT_CREATE);
        }
        Promotion promotion = promotionMapper.toPromotionFlashSale(request);
        promotion.setActive(false);
        promotion.setCampaign(promotionCampaign);
        if(request.getPromotionKind() != PromotionKind.FLASH_SALE && request.getApplyTo() != ApplyTo.Product){
            throw new AppException(CAN_NOT_CREATE_FALHSALE);
        }
        validateFlashSaleCreation(request);
        List<PromotionResponse> responses = new ArrayList<>();
        promotion.setPromotionApplyTo(new ArrayList<>());
        for(FlashSaleItemRequest item : request.getFlashSaleItemRequests()){
            if(item.getDiscountPercent() > 0.0 ){
                promotion.setDiscountPercent(item.getDiscountPercent());
                promotion.setDiscountType(DiscountType.DISCOUNT_PERCENT);
            }
            else if(item.getFixedAmount() > 0.0){
                promotion.setDiscountPercent(item.getDiscountPercent());
                promotion.setDiscountType(DiscountType.DISCOUNT_PERCENT);
            }
            promotion.getPromotionApplyTo().add(PromotionApplyTo.builder()
                            .promotion(promotion)
                            .productId(item.getProductId())
                    .build());
            promotion.setCreateAt(LocalDate.now());
            responses.add(promotionMapper.toPromotionResponse(promotionRepository.save(promotion)));
        }
        return responses;
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
    public PageResponse<PromotionResponse> getPromotionFlashSale(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = promotionRepository.findAllByPromotionKind(PromotionKind.FLASH_SALE, pageable);
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
    public List<PromotionResponse> getPromotionForOrder(List<String> skus, Double totalAmount, LocalDate today) {
        String userId = getUserId();
        var productResponse = searchClient.getProductDetails(skus);
        if (productResponse == null || productResponse.getCode() != 200 || productResponse.getResult() == null) {
            throw new RuntimeException("Cannot fetch product info");
        }
        Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                .collect(Collectors.toMap(ProductSkuVM::getSku, Function.identity()));
        List<String> productIds = productMap.values().stream()
                .map(ProductSkuVM::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<String> categoryIds = productMap.values().stream()
                .flatMap(p -> p.getCategoriesId() == null ? Stream.empty() : p.getCategoriesId().stream())
                .distinct()
                .toList();
        List<Promotion> promotionList = promotionRepository.findApplicablePromotions(
                today,
                totalAmount,
                productIds,
                categoryIds,
                userId
        );
        return promotionList.stream().map(promotionMapper::toPromotionResponse).toList();
    }

    @Override
    public PromotionEndingSoonResponse getListPromotionEndingSoon() {
        LocalDate today = LocalDate.now();
        List<Promotion> promotions = promotionRepository.findExpireToday(today);
        if(promotions == null){
            throw new AppException(PROMOTION_NOT_EXISTS);
        }
        LocalDateTime expiredAt =
                today.atTime(23, 59, 59);
        List<String> promotionIds = promotions.stream().map(Promotion::getId).toList();
        return PromotionEndingSoonResponse.builder()
                .promotionId(promotionIds)
                .expiredAt(expiredAt)
                .build();
    }

    @Override
        public PromotionResponse checkValidVoucher(CheckValidVoucherRequest request) {
            String userId = getUserId();

            Promotion promotion = promotionRepository
                    .findByVoucherCode(request.getVoucherCode());

            if (promotion == null) {
                throw new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS);
            }

            LocalDateTime now = request.getToday();

            if (now.isBefore(promotion.getStartDate())
                    || now.isAfter(promotion.getEndDate())) {
                throw new AppException(PromotionErrorCode.PROMOTION_EXPIRED);
            }

            if (request.getTotalAmount()
                    < promotion.getMinimumOrderPurchaseAmount()) {
                throw new AppException(PromotionErrorCode.PROMOTION_NOT_VALID_FOR_ORDER);
            }

            switch (promotion.getApplyTo()) {
                case ALL:
                    break;
                case Product:
                    if (request.getProductId() == null || request.getProductId().isEmpty()) {
                        throw new AppException(PromotionErrorCode.PROMOTION_NOT_VALID_FOR_ORDER);
                    }
                    boolean productMatched = promotion.getPromotionApplyTo()
                            .stream()
                            .map(PromotionApplyTo::getProductId)
                            .filter(Objects::nonNull)
                            .anyMatch(request.getProductId()::contains);

                    if (!productMatched) {
                        throw new AppException(PromotionErrorCode.PROMOTION_NOT_VALID_FOR_ORDER);
                    }
                    break;
                case Category:
                    if (request.getCategoryId() == null || request.getCategoryId().isEmpty()) {
                        throw new AppException(PromotionErrorCode.PROMOTION_NOT_VALID_FOR_ORDER);
                    }

                    boolean categoryMatched = promotion.getPromotionApplyTo()
                            .stream()
                            .map(PromotionApplyTo::getCategoryId)
                            .filter(Objects::nonNull)
                            .anyMatch(request.getCategoryId()::contains);

                    if (!categoryMatched) {
                        throw new AppException(PromotionErrorCode.PROMOTION_NOT_VALID_FOR_ORDER);
                    }
                    break;

                default:
                    throw new AppException(PromotionErrorCode.PROMOTION_NOT_VALID_FOR_ORDER);
            }

            long userUsage =
                    promotionUsageRepository
                            .countByPromotionIdAndUserId(
                                    promotion.getId(), userId
                            );

            if (promotion.getUsageLimitPerUser() > 0
                    && userUsage >= promotion.getUsageLimitPerUser()) {
                throw new AppException(PromotionErrorCode.PROMOTION_USED_LIMIT);
            }

            if (promotion.getUsageCount()
                    >= promotion.getUsageLimited()) {
                throw new AppException(PromotionErrorCode.PROMOTION_OUT_OF_QUOTA);
            }

            return PromotionResponse.builder()
                    .fixedAmount(promotion.getFixedAmount())
                    .discountPercent(promotion.getDiscountPercent())
                    .minimumOrderPurchaseAmount(
                            promotion.getMinimumOrderPurchaseAmount()
                    )
                    .maxDiscountAmount(promotion.getMaxDiscountAmount())
                    .build();
        }

    public void applyVoucherToOrder(
            String voucherCode,
            String orderId
    ) {
        String userId = getUserId();

        Promotion promotion = promotionRepository
                .findByVoucherCode(voucherCode);
        if (promotion == null) {
            throw new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS);
        }

        if (promotionUsageRepository
                .countByPromotionIdAndOrderId(
                        promotion.getId(), orderId
                ) > 0) {
            return;
        }

        if (promotion.getUsageCount()
                >= promotion.getUsageLimited()) {
            throw new AppException(PromotionErrorCode.PROMOTION_OUT_OF_QUOTA);
        }

        promotion.setUsageCount(
                promotion.getUsageCount() + 1
        );
        promotionRepository.save(promotion);

        promotionUsageRepository.save(
                PromotionUsage.builder()
                        .promotion(promotion)
                        .userId(userId)
                        .orderId(orderId)
                        .build()
        );
    }
    public void rollbackVoucher(String orderId) {

        PromotionUsage usage =
                promotionUsageRepository
                        .findByOrderId(orderId);

        if (usage == null) return;

        Promotion promotion = usage.getPromotion();
        promotion.setUsageCount(
                promotion.getUsageCount() - 1
        );

        promotionRepository.save(promotion);
        promotionUsageRepository.delete(usage);
    }

    @Override
    public void deletePromotion(String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(() ->
                new AppException(PromotionErrorCode.PROMOTION_NOT_EXISTS));
        if (Boolean.TRUE.equals(promotion.getActive())) {
            throw new AppException(PROMOTION_IS_ACTIVE);
        }
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
    public void UpdatePromotionStatus(Promotion promotion){
        StatusEvent statusEvent = StatusEvent.builder()
                .id(promotion.getId())
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

    @Override
    public void activatePromotion(Promotion promotion) {
        promotion.setActive(true);
        promotion.setUpdateAt(LocalDate.now());

        promotionRepository.save(promotion);
        if(promotion.getPromotionKind() != PromotionKind.VOUCHER) {
            sendKafKaEvent(promotion, "CREATED");
        }
    }

    private void validateFlashSaleCreation(FlashSaleCreationRequest request) {
        List<Promotion> activeFlashSales =
                promotionRepository.findActiveFlashSales();

        if (activeFlashSales.isEmpty()) {
            return;
        }
        boolean matchedTime = activeFlashSales.stream().anyMatch(p ->
                Objects.equals(p.getStartDate(), request.getStartDate()) &&
                        Objects.equals(p.getEndDate(), request.getEndDate())
        );
        if (!matchedTime) {
            throw new AppException(CAN_NOT_CREATE_FALHSALE);
        }
    }

    private void sendKafKaEvent(Promotion promotion, String eventType){
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
                        .campaignId(promotion.getCampaign().getId())
                        .descriptions(promotion.getDescriptions())
                        .active(promotion.getActive())
                        .promotionKind(promotion.getPromotionKind())
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
        }
    }

    private String getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult().getUserId();
    }
}
