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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryMapper inventoryMapper;
    private final InventoryTransactionMapper transactionMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void save(List<StockInItemRequest> request, String stockInId) {

        for (StockInItemRequest item : request) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProductId());
            if (inventory == null) {
                throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
            }
            if(inventory.getQuantity() == 0){
                productStockEvent(item.getProductId(), true);
            }
            int newQuantity = inventory.getQuantity() + item.getQuantity();
            inventory.setQuantity(newQuantity);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .inventory(inventory)
                    .productId(item.getProductId())
                    .transactionType(TransactionType.IN)
                    .quantity(item.getQuantity())
                    .referenceId(stockInId)
                    .referenceType(ReferenceType.STOCK_IN)
                    .note("Stock in product " + item.getProductId())
                    .createdAt(LocalDateTime.now())
                    .build();
            inventory.getTransactions().add(transaction);
            inventoryRepository.save(inventory);
        }
    }

    @Override
    public void createProduct(ProductEventDTO request) {
        Inventory newProduct = Inventory.builder()
                .productId(request.getId())
                .quantity(0)
                .build();
        inventoryRepository.save(newProduct);
    }

    @Override
    public boolean checkIsStock(IsInStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId());
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        return request.getQuantity() <= inventory.getQuantity();
    }

    @Override
    public void delete(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        productStockEvent(productId, false);
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryResponse getByProductId(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    public Inventory increaseStock(String productId, int quantity, String orderId) {

        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }

        boolean wasOutOfStock = inventory.getQuantity() == 0;

        inventory.setQuantity(inventory.getQuantity() + quantity);

        InventoryTransaction tran = InventoryTransaction.builder()
                .inventory(inventory)
                .productId(productId)
                .transactionType(TransactionType.IN)
                .quantity(quantity)
                .referenceType(ReferenceType.MANUAL)
                .referenceId(orderId)
                .note("Manual increase")
                .createdAt(LocalDateTime.now())
                .build();

        inventory.getTransactions().add(tran);

        if (wasOutOfStock) {
            productStockEvent(productId, true);
        }

        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory decreaseStock(String productId, int quantity, String orderId) {

        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }

        if (quantity > inventory.getQuantity()) {
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_ENOUGH);
        }

        int oldQty = inventory.getQuantity();
        int newQty = oldQty - quantity;
        inventory.setQuantity(newQty);

        InventoryTransaction tran = InventoryTransaction.builder()
                .inventory(inventory)
                .productId(productId)
                .transactionType(TransactionType.OUT)
                .quantity(quantity)
                .referenceType(ReferenceType.ORDER)
                .referenceId(orderId)
                .note("Decrease stock")
                .createdAt(LocalDateTime.now())
                .build();

        inventory.getTransactions().add(tran);

        if (newQty == 0) {
            productStockEvent(productId, false);
        }

        return inventoryRepository.save(inventory);
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

    private void productStockEvent(String productId, Boolean isInStock){
        ChangeStatusStockEvent event = ChangeStatusStockEvent.builder()
                .productId(productId)
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
}
