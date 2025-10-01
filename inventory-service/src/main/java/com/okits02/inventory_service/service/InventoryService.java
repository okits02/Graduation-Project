package com.okits02.inventory_service.service;

import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.model.Inventory;

public interface InventoryService {
    public InventoryResponse save(InventoryRequest request);
    public InventoryResponse update(InventoryRequest request);
    public boolean checkIsStock(IsInStockRequest request);
    public void delete(String productId);
    public InventoryResponse getByProductId(String productId);
    public Inventory decreaseStock(String productId, int quantity);
    public Inventory increaseStock(String productId, int quantity);

}
