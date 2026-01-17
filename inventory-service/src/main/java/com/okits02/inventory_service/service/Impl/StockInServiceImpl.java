package com.okits02.inventory_service.service.Impl;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.inventory_service.dto.request.StockInCreationRequest;
import com.okits02.inventory_service.dto.request.StockInItemRequest;
import com.okits02.inventory_service.dto.response.ProductVariantResponse;
import com.okits02.inventory_service.dto.response.StockInItemResponse;
import com.okits02.inventory_service.dto.response.StockInResponse;
import com.okits02.inventory_service.enums.EventType;
import com.okits02.inventory_service.exceptions.StockInErrorCode;
import com.okits02.inventory_service.kafka.StockInAnalysisEvent;
import com.okits02.inventory_service.kafka.StockInItemEvent;
import com.okits02.inventory_service.mapper.StockInItemMapper;
import com.okits02.inventory_service.mapper.StockInMapper;
import com.okits02.inventory_service.model.StockIn;
import com.okits02.inventory_service.model.StockInItem;
import com.okits02.inventory_service.repository.StockInRepository;
import com.okits02.inventory_service.repository.httpClient.SearchClient;
import com.okits02.inventory_service.service.InventoryService;
import com.okits02.inventory_service.service.StockInService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockInServiceImpl implements StockInService {
    private final StockInRepository stockInRepository;
    private final InventoryService inventoryService;
    private final StockInMapper stockInMapper;
    private final StockInItemMapper stockInItemMapper;
    private final SearchClient searchClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Override
    @Transactional
    public StockInResponse save(StockInCreationRequest request) {
        if(stockInRepository.existsByReferenceCode(request.getReferenceCode())){
            throw new AppException(StockInErrorCode.STOCK_IN_EXISTS_BY_REFERENCE_CODE);
        }
        StockIn newStockIn = stockInMapper.toStockIn(request);
        List<StockInItem> items = createItemStockIn(newStockIn, request.getItems());
        newStockIn.setItems(items);
        newStockIn = stockInRepository.save(newStockIn);
        inventoryService.save(request.getItems(), newStockIn.getId());
        sendStockInAnalysisEvent(newStockIn, EventType.CREATE);
        List<String> skus = items.stream()
                .map(StockInItem::getSku)
                .distinct()
                .toList();
        Map<String, ProductVariantResponse> variantMap =
                fetchVariantsBySku(skus);
        List<StockInItemResponse> itemResponses =
                items.stream()
                        .map(item -> {
                            StockInItemResponse res =
                                    stockInItemMapper.toResponse(item);

                            ProductVariantResponse variant =
                                    variantMap.get(item.getSku());

                            if (variant != null) {
                                res.setVariantName(variant.getVariantName());
                                res.setThumbnail(variant.getThumbnailUrl());
                                res.setColor(variant.getColor());
                            }
                            return res;
                        })
                        .toList();
        return StockInResponse.builder()
                .referenceCode(newStockIn.getReferenceCode())
                .supplierName(newStockIn.getSupplierName())
                .totalAmount(newStockIn.getTotalAmount())
                .createAt(newStockIn.getCreatedAt())
                .items(itemResponses)
                .note(newStockIn.getNote())
                .build();
    }

    @Override
    public StockInResponse getByReferenceCode(String referenceCode) {
        Optional<StockIn> stockIn = stockInRepository.findByReferenceCode(referenceCode);
        if(stockIn.get() == null){
            throw new AppException(StockInErrorCode.STOCK_IN_NOT_EXISTS);
        }
        List<StockInItem> items = stockIn.get().getItems();
        List<String> skus = items.stream()
                .map(StockInItem::getSku)
                .distinct()
                .toList();
        Map<String, ProductVariantResponse> variantMap =
                fetchVariantsBySku(skus);
        List<StockInItemResponse> itemResponses =
                items.stream()
                        .map(item -> {
                            StockInItemResponse res =
                                    stockInItemMapper.toResponse(item);

                            ProductVariantResponse variant =
                                    variantMap.get(item.getSku());

                            if (variant != null) {
                                res.setVariantName(variant.getVariantName());
                                res.setThumbnail(variant.getThumbnailUrl());
                                res.setColor(variant.getColor());
                            }
                            return res;
                        })
                        .toList();
        return StockInResponse.builder()
                .referenceCode(stockIn.get().getReferenceCode())
                .supplierName(stockIn.get().getSupplierName())
                .totalAmount(stockIn.get().getTotalAmount())
                .createAt(stockIn.get().getCreatedAt())
                .items(itemResponses)
                .note(stockIn.get().getNote())
                .build();
    }

    @Override
    public PageResponse<StockInResponse> getAllHistory(int page, int size, LocalDateTime start, LocalDateTime end) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = stockInRepository.getAllHistory(start, end, pageable);
        List<StockIn> stockIns = pageData.getContent();
        List<String> skus = stockIns.stream()
                .flatMap(stockIn -> stockIn.getItems().stream())
                .map(StockInItem::getSku)
                .distinct()
                .toList();
        Map<String, ProductVariantResponse> variantMap =
                fetchVariantsBySku(skus);
        List<StockInResponse> stockInResponses =
                stockIns.stream()
                        .map(stockIn -> {

                            List<StockInItemResponse> itemResponses =
                                    stockIn.getItems().stream()
                                            .map(item -> {
                                                StockInItemResponse res =
                                                        stockInItemMapper.toResponse(item);

                                                ProductVariantResponse variant =
                                                        variantMap.get(item.getSku());

                                                if (variant != null) {
                                                    res.setVariantName(variant.getVariantName());
                                                    res.setThumbnail(variant.getThumbnailUrl());
                                                    res.setColor(variant.getColor());
                                                }
                                                return res;
                                            })
                                            .toList();

                            return StockInResponse.builder()
                                    .supplierName(stockIn.getSupplierName())
                                    .referenceCode(stockIn.getReferenceCode())
                                    .note(stockIn.getNote())
                                    .totalAmount(stockIn.getTotalAmount())
                                    .createAt(stockIn.getCreatedAt())
                                    .items(itemResponses)
                                    .build();
                        })
                        .toList();

        return PageResponse.<StockInResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(stockInResponses)
                .build();
    }

    @Override
    public void deleteStockIn(String referenceCode) {
        Optional<StockIn> stockIn = stockInRepository.findByReferenceCode(referenceCode);
        if(stockIn.get() == null){
            throw new AppException(StockInErrorCode.STOCK_IN_NOT_EXISTS);
        }
        sendStockInAnalysisEvent(stockIn.get(), EventType.DELETE);
        stockInRepository.delete(stockIn.get());
    }

    @Override
    public StockInResponse getById(String stockInId) {
        Optional<StockIn> stockIn = stockInRepository.findById(stockInId);
        if(stockIn.get() ==  null){
            throw new AppException(StockInErrorCode.STOCK_IN_NOT_EXISTS);
        }
        List<StockInItem> items = stockIn.get().getItems();
        List<String> skus = items.stream()
                .map(StockInItem::getSku)
                .distinct()
                .toList();
        Map<String, ProductVariantResponse> variantMap =
                fetchVariantsBySku(skus);
        List<StockInItemResponse> itemResponses =
                items.stream()
                        .map(item -> {
                            StockInItemResponse res =
                                    stockInItemMapper.toResponse(item);

                            ProductVariantResponse variant =
                                    variantMap.get(item.getSku());

                            if (variant != null) {
                                res.setVariantName(variant.getVariantName());
                                res.setThumbnail(variant.getThumbnailUrl());
                                res.setColor(variant.getColor());
                            }
                            return res;
                        })
                        .toList();
        return StockInResponse.builder()
                .referenceCode(stockIn.get().getReferenceCode())
                .supplierName(stockIn.get().getSupplierName())
                .totalAmount(stockIn.get().getTotalAmount())
                .createAt(stockIn.get().getCreatedAt())
                .items(itemResponses)
                .note(stockIn.get().getNote())
                .build();
    }

    private List<StockInItem> createItemStockIn(StockIn stockIn, List<StockInItemRequest> request){
        List<StockInItem> items = new ArrayList<>();
        for(StockInItemRequest item : request){
            StockInItem stockInItem = StockInItem.builder()
                    .sku(item.getSku())
                    .quantity(item.getQuantity())
                    .unitCost(item.getUnitCost())
                    .stockIn(stockIn)
                    .build();
            items.add(stockInItem);
        }
        return items;
    }


    private Map<String, ProductVariantResponse> fetchVariantsBySku(List<String> skus) {

        if (skus == null || skus.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            var response = searchClient.getVariantBySku(skus);

            if (response == null || response.getResult() == null) {
                return Collections.emptyMap();
            }

            return response.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductVariantResponse::getSku,
                            v -> v
                    ));

        } catch (Exception e) {
            log.warn("Cannot fetch variants by skus {}", skus, e);
            return Collections.emptyMap();
        }
    }

    private void sendStockInAnalysisEvent(StockIn stockIn, EventType eventType){
        if(stockIn == null){
            log.warn("stockIn is null, skip sending analysis event");
            return;
        }
        try {
            List<String> skus = stockIn.getItems().stream().map(StockInItem::getSku).toList();
            var response = searchClient.getVariantBySku(skus);
            Map<String, ProductVariantResponse> variantMap = response.getResult()
                    .stream()
                    .collect(Collectors.toMap(
                            ProductVariantResponse::getSku,
                            Function.identity()
                    ));
            StockInAnalysisEvent stockInAnalysisEvent = StockInAnalysisEvent.builder()
                    .id(stockIn.getId())
                    .referenceCode(stockIn.getReferenceCode())
                    .totalAmount(stockIn.getTotalAmount())
                    .supplierName(stockIn.getSupplierName())
                    .items(stockIn.getItems().stream()
                            .map(item -> {
                                ProductVariantResponse variant = variantMap.get(item.getSku());

                                return StockInItemEvent.builder()
                                        .id(item.getId())
                                        .sku(item.getSku())
                                        .variantName(variant != null ? variant.getVariantName() : null)
                                        .thumbnail(variant != null ? variant.getThumbnailUrl() : null)
                                        .quantity(item.getQuantity())
                                        .unitCost(item.getUnitCost())
                                        .totalCost(item.getTotalCost())
                                        .build();
                            })
                            .toList()
                    )
                    .createdAt(stockIn.getCreatedAt())
                    .build();
            switch (eventType){
                case CREATE -> {
                    stockInAnalysisEvent.setEventType(EventType.CREATE);
                    kafkaTemplate.send("stockIn-analysis-event", stockInAnalysisEvent).whenComplete((result, ex)
                            -> {
                        if (ex != null) {
                            log.error(
                                    "Failed to send StockInAnalysisEvent, txId={}",
                                    stockIn.getId(),
                                    ex);
                        } else {
                            log.info(
                                    "TransactionAnalysisEvent sent successfully: {}",
                                    result.getProducerRecord()
                            );
                        }
                    });
                }
                case DELETE -> {
                    stockInAnalysisEvent.setEventType(EventType.DELETE);
                    kafkaTemplate.send("stockIn-analysis-event", stockInAnalysisEvent).whenComplete((result, ex)
                            -> {
                        if (ex != null) {
                            log.error(
                                    "Failed to send StockInAnalysisEvent, txId={}",
                                    stockIn.getId(),
                                    ex);
                        } else {
                            log.info(
                                    "TransactionAnalysisEvent sent successfully: {}",
                                    result.getProducerRecord()
                            );
                        }
                    });
                }
            }
        }catch (Exception e) {
            log.error(
                    "Unexpected error when sending StockIn, txId={}",
                    stockIn.getId()
            );
        }
    }

}
