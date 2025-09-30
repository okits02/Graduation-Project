package com.okits02.inventory_service.service;

import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;

public interface InventoryService {
    public InventoryResponse save(InventoryRequest request);
    public InventoryResponse update(InventoryRequest request);
    public boolean checkIsStock(IsInStockRequest request);
    public void Delete(String productId);
    public InventoryResponse getByProductId(String productId);
    public void decreaseStock(String productId, int quantity);
    public void increaseStock(String productId, int quantity);

}
