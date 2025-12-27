package com.okits02.inventory_service.service.Impl;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.ProductEventDTO;
import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.request.StockInItemRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.inventory_service.dto.response.InventoryTransactionResponse;
import com.okits02.inventory_service.enums.ReferenceType;
import com.okits02.inventory_service.enums.TransactionType;
import com.okits02.inventory_service.exceptions.InventoryErrorCode;
import com.okits02.inventory_service.kafka.ChangeStatusStockEvent;
import com.okits02.inventory_service.mapper.InventoryMapper;
import com.okits02.inventory_service.mapper.InventoryTransactionMapper;
import com.okits02.inventory_service.model.Inventory;
import com.okits02.inventory_service.model.InventoryTransaction;
import com.okits02.inventory_service.repository.InventoryRepository;
import com.okits02.inventory_service.repository.InventoryTransactionRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Override
    public void save(List<StockInItemRequest> request, String stockInId) {
        List<String> touchedProductIds = new ArrayList<>();
        for (StockInItemRequest item : request) {
            Inventory inventory = inventoryRepository.findByProductIdAndSku(item.getProductId(),
                    item.getSku()).orElseThrow(() ->
                    new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));
            boolean wasInStock = isProductInStock(item.getProductId());
            if(inventory.getQuantity() == 0){
                productStockEvent(item.getProductId(), item.getSku(),true);
            }
            int newQuantity = inventory.getQuantity() + item.getQuantity();
            inventory.setQuantity(newQuantity);

            InventoryTransaction tx = buildTransaction(
                    inventory,
                    item.getProductId(),
                    item.getSku(),
                    TransactionType.IN,
                    item.getQuantity(),
                    stockInId,
                    ReferenceType.STOCK_IN,
                    "Stock in product " + item.getProductId() + " sku " + item.getSku()
            );
            applyTransaction(inventory, tx);

            inventoryRepository.save(inventory);

            if (!touchedProductIds.contains(item.getProductId())) {
                touchedProductIds.add(item.getProductId());
            }
            boolean nowInStock = isProductInStock(item.getSku());
            if (wasInStock != nowInStock) {
                productStockEvent(item.getProductId(), item.getSku(), nowInStock);
            }
        }
    }

    @Override
    public void createProduct(ProductEventDTO request) {
        if (request == null || request.getProductVariants() == null || request.getProductVariants().isEmpty()) {
            return;
        }

        String productId = request.getId();
        String productName = request.getName();
        for(var variant : request.getProductVariants()){
            if (variant == null || isBlank(variant.getSku())) continue;
            String sku = variant.getSku();
            String fullName = buildFullName(productName, variant.getVariantName());

            inventoryRepository.findByProductIdAndSku(productId, sku).ifPresentOrElse(
                    existing -> {
                        existing.setProductName(fullName);
                        existing.setUpdatedAt(LocalDateTime.now());
                        inventoryRepository.save(existing);
                    }, () -> {
                        Inventory inv = Inventory.builder()
                                .productId(productId)
                                .sku(sku)
                                .productName(fullName)
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
        if (request == null || isBlank(request.getProductId()) || isBlank(request.getSku()) || request.getQuantity() == null) {
            return false;
        }
        Inventory inventory = inventoryRepository.findByProductIdAndSku(request.getProductId(), request.getSku())
                .orElseThrow(() -> new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));

        return request.getQuantity() <= inventory.getQuantity();
    }

    @Override
    public void delete(String productId) {

        boolean wasInStock = isProductInStock(productId);

        List<Inventory> inventories = inventoryRepository.findAllByProductId(productId);
        if (inventories == null || inventories.isEmpty()) {
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }

        inventoryRepository.deleteAll(inventories);
        if (wasInStock) {
            productStockEvent(productId, "",false);
        }
    }

    @Override
    public InventoryResponse getByProductIdAndSku(String productId, String sku) {
        Optional<Inventory> inventory = inventoryRepository.findByProductIdAndSku(productId, sku);
        log.info(inventory.toString());
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        InventoryResponse inventoryResponse = inventoryMapper.toInventoryResponse(inventory.get());
        log.info(inventoryResponse.toString());
        return inventoryMapper.toInventoryResponse(inventory.get());
    }

    @Override
    public Inventory increaseStock(String productId, String sku, int quantity, String orderId) {

        Inventory inventory = inventoryRepository.findByProductIdAndSku(productId, sku).orElseThrow(() ->
            new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS)
        );
        boolean wasInStock = isProductInStock(sku);


        InventoryTransaction tx = buildTransaction(
                inventory,
                productId,
                sku,
                TransactionType.OUT,
                quantity,
                orderId,
                ReferenceType.ORDER,
                "Manual increase");
        applyTransaction(inventory, tx);
        Inventory saved = inventoryRepository.save(inventory);

        boolean nowInStock = isProductInStock(sku);
        if (wasInStock != nowInStock) {
            productStockEvent(productId, sku, nowInStock);
        }
        return saved;
    }

    @Override
    public Inventory decreaseStock(String productId, String sku, int quantity, String orderId) {

        Inventory inventory = inventoryRepository.findByProductIdAndSku(productId, sku).orElseThrow(() ->
                new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS));
        boolean wasInStock = isProductInStock(productId);

        if(quantity > inventory.getQuantity()){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_ENOUGH);
        }

        InventoryTransaction tx = buildTransaction(
                inventory,
                productId,
                sku,
                TransactionType.OUT,
                quantity,
                orderId,
                ReferenceType.ORDER,
                "Decrease stock"
        );
        Inventory saved = inventoryRepository.save(inventory);

        boolean nowInStock = isProductInStock(sku);
        if (wasInStock != nowInStock) {
            productStockEvent(productId, sku,nowInStock);
        }
        return saved;
    }

    @Override
    public PageResponse<InventoryResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = inventoryRepository.findAll(pageable);
        return PageResponse.<InventoryResponse>builder()
                .currentPage(page)
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream().map(inventoryMapper::toInventoryResponse).toList())
                .build();
    }

    @Override
    public PageResponse<InventoryTransactionResponse> getTransactionHistory(String productId, int page, int size) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<InventoryTransaction> pageData =
                inventoryTransactionRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);

        return PageResponse.<InventoryTransactionResponse>builder()
                .currentPage(pageData.getNumber())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .data(pageData.getContent().stream()
                        .map(transactionMapper::toResponse)
                        .toList())
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
            String productId,
            String sku,
            TransactionType type,
            int quantity,
            String referenceId,
            ReferenceType referenceType,
            String note
    ) {
        return InventoryTransaction.builder()
                .inventory(inventory)
                .productId(productId)
                .sku(sku)
                .transactionType(type)
                .quantity(Math.abs(quantity))
                .referenceId(referenceId)
                .referenceType(referenceType)
                .note(note)
                .createdAt(LocalDateTime.now())
                .build();
    }
    private boolean isProductInStock(String sku) {
        return inventoryRepository.existsBySkuAndQuantityGreaterThan(sku, 0);
    }
    private void productStockEvent(String productId, String sku, Boolean isInStock){
        ChangeStatusStockEvent event = ChangeStatusStockEvent.builder()
                .productId(productId)
                .sku(sku)
                .inStock(isInStock)
                .build();
        kafkaTemplate.send("change-status-event", event).whenComplete(
                (result, ex) ->{
                    if (ex != null)
                    {
                        System.err.println("Failed to send message" + ex.getMessage());
                    } else {
                        System.err.println("send message successfully" + result.getProducerRecord());
                    }
                });
    }

    private String buildFullName(String productName, String variantName) {
        if (isBlank(variantName)) return productName;
        return productName + " - " + variantName;
    }
}
