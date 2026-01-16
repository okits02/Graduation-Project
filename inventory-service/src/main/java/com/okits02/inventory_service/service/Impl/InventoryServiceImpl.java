package com.okits02.inventory_service.service.Impl;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.ProductEventDTO;
import com.okits02.inventory_service.dto.ProductVariantsEventDTO;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.request.StockInItemRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.inventory_service.dto.response.InventoryTransactionResponse;
import com.okits02.inventory_service.dto.response.ProductVariantResponse;
import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import com.okits02.inventory_service.exceptions.InventoryErrorCode;
import com.okits02.inventory_service.kafka.ChangeStatusStockEvent;
import com.okits02.inventory_service.kafka.TransactionAnalysisEvent;
import com.okits02.inventory_service.mapper.InventoryMapper;
import com.okits02.inventory_service.mapper.InventoryTransactionMapper;
import com.okits02.inventory_service.model.Inventory;
import com.okits02.inventory_service.model.InventoryTransaction;
import com.okits02.inventory_service.repository.InventoryRepository;
import com.okits02.inventory_service.repository.InventoryTransactionRepository;
import com.okits02.inventory_service.repository.httpClient.SearchClient;
import com.okits02.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isBlank;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SearchClient searchClient;

    @Override
    public void save(List<StockInItemRequest> request, String stockInId) {
        List<String> touchedProductIds = new ArrayList<>();
        for (StockInItemRequest item : request) {
            Inventory inventory = inventoryRepository.findBySku(item.getSku()).orElseThrow(() ->
                    new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));
            boolean wasInStock = isProductInStock(item.getSku());

            InventoryTransaction tx = buildTransaction(
                    inventory,
                    item.getSku(),
                    TransactionType.IN,
                    item.getQuantity(),
                    stockInId,
                    ReferenceType.STOCK_IN,
                    "Stock in product " + item.getSku()
            );
            applyTransaction(inventory, tx);

            inventoryRepository.save(inventory);

            if (!touchedProductIds.contains(item.getSku())) {
                touchedProductIds.add(item.getSku());
            }
            boolean nowInStock = isProductInStock(item.getSku());
            if (wasInStock != nowInStock) {
                productStockEvent( item.getSku(), nowInStock);
            }
        }
    }

    @Override
    public void createProduct(ProductEventDTO request) {
        if (request == null || request.getProductVariants() == null || request.getProductVariants().isEmpty()) {
            return;
        }

        String productId = request.getId();
        for(var variant : request.getProductVariants()){
            if (variant == null || isBlank(variant.getSku())) continue;
            String sku = variant.getSku();

            inventoryRepository.findBySku(sku).ifPresentOrElse(
                    existing -> {
                        existing.setUpdatedAt(LocalDateTime.now());
                        inventoryRepository.save(existing);
                    }, () -> {
                        Inventory inv = Inventory.builder()
                                .sku(sku)
                                .quantity(0)
                                .transactions(new ArrayList<>())
                                .build();
                        try {
                            inventoryRepository.save(inv);
                        }catch (DataIntegrityViolationException e) {
                            log.warn("Inventory already exists for productId={} sku={}, ignore duplicate create", productId, sku);
                        }
                    }
            );
        }
    }

    @Override
    public boolean checkIsStock(IsInStockRequest request) {
        if (request == null || isBlank(request.getSku()) || request.getQuantity() == null) {
            return false;
        }
        Inventory inventory = inventoryRepository.findBySku(request.getSku())
                .orElseThrow(() -> new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));

        return request.getQuantity() <= inventory.getQuantity();
    }

    @Override
    public void delete(List<ProductVariantsEventDTO> productVariantsEventDTOS) {
        if(productVariantsEventDTOS == null || productVariantsEventDTOS.isEmpty()) return;
        for (ProductVariantsEventDTO variantsEventDTO : productVariantsEventDTOS) {
            boolean wasInStock = isProductInStock(variantsEventDTO.getSku());

            Inventory inventory = inventoryRepository.findBySku(variantsEventDTO.getSku()).orElseThrow(()
                    -> new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));

            inventoryRepository.delete(inventory);
            if (wasInStock) {
                productStockEvent(variantsEventDTO.getSku(), false);
            }
        }
    }

    @Override
    public InventoryResponse getByProductIdAndSku(String sku) {

        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));

        InventoryResponse response =
                inventoryMapper.toInventoryResponse(inventory);

        Map<String, ProductVariantResponse> variantMap =
                fetchVariantsBySku(List.of(sku));

        ProductVariantResponse variant = variantMap.get(sku);
        if (variant != null) {
            response.setVariantName(variant.getVariantName());
            response.setThumbnail(variant.getThumbnailUrl());
            response.setColor(variant.getColor());
        }

        return response;
    }

    @Override
    public Inventory increaseStock(String sku, int quantity, String orderId) {

        Inventory inventory = inventoryRepository.findBySku(sku).orElseThrow(() ->
            new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS)
        );
        boolean wasInStock = isProductInStock(sku);


        InventoryTransaction tx = buildTransaction(
                inventory,
                sku,
                TransactionType.RETURN,
                quantity,
                orderId,
                ReferenceType.ORDER,
                "Manual increase");
        applyTransaction(inventory, tx);
        Inventory saved = inventoryRepository.save(inventory);

        boolean nowInStock = isProductInStock(sku);
        if (wasInStock != nowInStock) {
            productStockEvent(sku, nowInStock);
        }
        return saved;
    }

    @Override
    public Inventory decreaseStock(String sku, int quantity, String orderId) {

        Inventory inventory = inventoryRepository.findBySku(sku).orElseThrow(() ->
                new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));
        boolean wasInStock = isProductInStock(sku);

        if(quantity > inventory.getQuantity()){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_ENOUGH);
        }

        InventoryTransaction tx = buildTransaction(
                inventory,
                sku,
                TransactionType.OUT,
                quantity,
                orderId,
                ReferenceType.ORDER,
                "Decrease stock"
        );
        applyTransaction(inventory, tx);
        Inventory saved = inventoryRepository.save(inventory);

        boolean nowInStock = isProductInStock(sku);
        if (wasInStock != nowInStock) {
            productStockEvent(sku,nowInStock);
        }
        return saved;
    }

    @Override
    public PageResponse<InventoryResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = inventoryRepository.findAll(pageable);
        List<String> skus = pageData.getContent().stream().map(Inventory::getSku).toList();
        var variantMap = fetchVariantsBySku(skus);
        List<InventoryResponse> responses = pageData.getContent().stream()
                .map(inv -> {
                    InventoryResponse res = inventoryMapper.toInventoryResponse(inv);
                    ProductVariantResponse variant = variantMap.get(inv.getSku());
                    if (variant != null) {
                        res.setVariantName(variant.getVariantName());
                        res.setThumbnail(variant.getThumbnailUrl());
                        res.setColor(variant.getColor());
                    }
                    return res;
                }).toList();
        return PageResponse.<InventoryResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(responses)
                .build();
    }

    @Override
    public Map<String, Long> getTotalSold(List<String> skus) {
        if (skus == null || skus.isEmpty()) return Map.of();
        List<Object[]> rows = inventoryTransactionRepository.sumSoldGroupBySku(
                skus, TransactionType.OUT, ReferenceType.ORDER
        );

        Map<String, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            String sku = (String) r[0];
            Long sold = (Long) r[1];
            map.put(sku, sold == null ? 0L : sold);
        }
        for (String sku : skus) map.putIfAbsent(sku, 0L);

        return map;
    }


    @Override
    public PageResponse<InventoryTransactionResponse> getTransactionHistory(String sku, int page, int size) {
        Inventory inventory = inventoryRepository.findBySku(sku).orElseThrow(() ->
                new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));

        Pageable pageable = PageRequest.of(page, size);

        Page<InventoryTransaction> pageData =
                inventoryTransactionRepository.findBySkuOrderByCreatedAtDesc(sku, pageable);
        List<InventoryTransaction> transactions = pageData.getContent();
        List<String> skus = transactions.stream()
                .map(InventoryTransaction::getSku)
                .distinct()
                .toList();
        Map<String, ProductVariantResponse> variantMap =
                fetchVariantsBySku(skus);

        List<InventoryTransactionResponse> responses =
                transactions.stream()
                        .map(tx -> {
                            InventoryTransactionResponse res =
                                    transactionMapper.toResponse(tx);

                            ProductVariantResponse variant =
                                    variantMap.get(tx.getSku());

                            if (variant != null) {
                                res.setVariantName(variant.getVariantName());
                                res.setThumbnail(variant.getThumbnailUrl());
                                res.setColor(variant.getColor());
                            }
                            return res;
                        })
                        .toList();
        return PageResponse.<InventoryTransactionResponse>builder()
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(responses)
                .build();
    }

    private void applyTransaction(Inventory inventory, InventoryTransaction tx) {
        if (inventory.getTransactions() == null) {
            inventory.setTransactions(new ArrayList<>());
        }

        inventory.getTransactions().add(tx);

        int delta = (tx.getTransactionType() == TransactionType.IN) ? tx.getQuantity() : -tx.getQuantity();
        inventory.setQuantity(inventory.getQuantity() + delta);
        inventory.setUpdatedAt(LocalDateTime.now());
    }

    private InventoryTransaction buildTransaction(
            Inventory inventory,
            String sku,
            TransactionType type,
            int quantity,
            String referenceId,
            ReferenceType referenceType,
            String note
    ) {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .inventory(inventory)
                .sku(sku)
                .transactionType(type)
                .quantity(Math.abs(quantity))
                .referenceId(referenceId)
                .referenceType(referenceType)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();
        sendTransactionAnalysisEvent(transaction);
        return transaction;
    }
    private boolean isProductInStock(String sku) {
        return inventoryRepository.existsBySkuAndQuantityGreaterThan(sku, 0);
    }
    private void productStockEvent(String sku, Boolean isInStock){
        try {
            ChangeStatusStockEvent event = ChangeStatusStockEvent.builder()
                    .sku(sku)
                    .inStock(isInStock)
                    .build();

            kafkaTemplate.send("change-status-event", event).whenComplete(
                    (result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send ChangeStatusStockEvent for sku={}", sku, ex);
                        } else {
                            log.info("ChangeStatusStockEvent sent successfully: {}",
                                    result.getProducerRecord());
                        }
                    }
            );
        } catch (Exception e) {
            log.error("Unexpected error when sending ChangeStatusStockEvent for sku={}", sku, e);
        }
    }

    private void sendTransactionAnalysisEvent(InventoryTransaction transaction){
        if (transaction == null) {
            log.warn("InventoryTransaction is null, skip sending analysis event");
            return;
        }

        try {
            TransactionAnalysisEvent transactionAnalysisEvent =
                    TransactionAnalysisEvent.builder()
                            .id(transaction.getId())
                            .sku(transaction.getSku())
                            .quantity(transaction.getQuantity())
                            .referenceId(transaction.getReferenceId())
                            .referenceType(transaction.getReferenceType())
                            .transactionType(transaction.getTransactionType())
                            .createdAt(transaction.getCreatedAt())
                            .build();

            kafkaTemplate.send("transaction-analysis-event", transactionAnalysisEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error(
                                    "Failed to send TransactionAnalysisEvent, txId={}, sku={}",
                                    transaction.getId(),
                                    transaction.getSku(),
                                    ex
                            );
                        } else {
                            log.info(
                                    "TransactionAnalysisEvent sent successfully: {}",
                                    result.getProducerRecord()
                            );
                        }
                    });

        } catch (Exception e) {
            log.error(
                    "Unexpected error when sending TransactionAnalysisEvent, txId={}, sku={}",
                    transaction.getId(),
                    transaction.getSku(),
                    e
            );
        }
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
}
