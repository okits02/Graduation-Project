package com.okits02.inventory_service.service;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.ProductEventDTO;
import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.request.StockInItemRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.dto.response.InventoryTransactionResponse;
import com.okits02.inventory_service.model.Inventory;

import java.util.List;

public interface InventoryService {
    public void save(List<StockInItemRequest> request, String stockInId);
    public void createProduct(ProductEventDTO request);
    public boolean checkIsStock(IsInStockRequest request);
    public void delete(String productId);
    public InventoryResponse getByProductId(String productId);
    public Inventory decreaseStock(String productId, int quantity);
    public Inventory increaseStock(String productId, int quantity);
    public PageResponse<InventoryResponse> getAll(int page, int size);
    PageResponse<InventoryTransactionResponse> getTransactionHistory(String productId, int page, int size);
}
