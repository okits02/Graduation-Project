package com.okits02.inventory_service.service.Impl;

import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.inventory_service.exceptions.InventoryErrorCode;
import com.okits02.inventory_service.kafka.ChangeStatusStockEvent;
import com.okits02.inventory_service.mapper.InventoryMapper;
import com.okits02.inventory_service.model.Inventory;
import com.okits02.inventory_service.repository.InventoryRepository;
import com.okits02.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public InventoryResponse save(InventoryRequest request) {
        if(inventoryRepository.existsByProductId(request.getProductId())){
            throw new AppException(InventoryErrorCode.PRODUCT_EXISTS);
        }
        Inventory newInventory = inventoryMapper.toInventory(request);
        productStockEvent(request.getProductId(), true);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(newInventory));
    }

    @Override
    public InventoryResponse update(InventoryRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId());
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        if(inventory.getQuantity() == 0){
            productStockEvent(request.getProductId(), true);
        }
        inventoryMapper.updateInventory(inventory, request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
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
    public Inventory decreaseStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        if(quantity > inventory.getQuantity()){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_ENOUGH);
        }
        if(quantity == inventory.getQuantity()){
            inventory.setQuantity(0);
            productStockEvent(productId, false);
        }else if(quantity < inventory.getQuantity()){
            inventory.setQuantity(inventory.getQuantity() - quantity);
        }

        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory increaseStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(InventoryErrorCode.PRODUCT_NOT_EXISTS);
        }
        if(inventory.getQuantity() == 0){
            productStockEvent(productId, true);
        }
        inventory.setQuantity(inventory.getQuantity() + quantity);
        return inventoryRepository.save(inventory);
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
