package com.okits02.inventory_service.service.Impl;

import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.exceptions.AppException;
import com.okits02.inventory_service.exceptions.ErrorCode;
import com.okits02.inventory_service.mapper.InventoryMapper;
import com.okits02.inventory_service.model.Inventory;
import com.okits02.inventory_service.repository.InventoryRepository;
import com.okits02.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.BreakIterator;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;
    @Override
    public InventoryResponse save(InventoryRequest request) {
        Inventory inventory = inventoryMapper.toInventory(request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Override
    public InventoryResponse update(InventoryRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId());
        if(inventory == null){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        inventoryMapper.updateInventory(inventory, request);
        return inventoryMapper.toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Override
    public boolean checkIsStock(IsInStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId());
        if(inventory == null){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        if(request.getQuantity() < inventory.getQuantity()){
            return true;
        }
        return false;
    }

    @Override
    public void delete(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        inventoryRepository.delete(inventory);
    }

    @Override
    public InventoryResponse getByProductId(String productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        return inventoryMapper.toInventoryResponse(inventory);
    }

    @Override
    public Inventory decreaseStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory increaseStock(String productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if(inventory == null){
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTS);
        }
        inventory.setQuantity(inventory.getQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

}
