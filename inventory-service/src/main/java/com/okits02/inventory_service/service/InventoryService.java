package com.okits02.inventory_service.service;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.ProductEventDTO;
import com.okits02.inventory_service.dto.ProductVariantsEventDTO;
import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.inventory_service.dto.request.StockInItemRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.dto.response.InventoryTransactionResponse;
import com.okits02.inventory_service.model.Inventory;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    public void save(List<StockInItemRequest> request, String stockInId);
    public void createProduct(ProductEventDTO request);
    public boolean checkIsStock(IsInStockRequest request);
    public void delete(List<ProductVariantsEventDTO> productVariantsEventDTOS);
    public InventoryResponse getByProductIdAndSku(String sku);
    public Inventory decreaseStock(String sku, int quantity, String orderId);
    public Inventory increaseStock(String sku, int quantity, String orderId);
    public PageResponse<InventoryResponse> getAll(int page, int size);
    public Map<String, Long> getTotalSold(List<String> skus);
    PageResponse<InventoryTransactionResponse> getTransactionHistory(String sku, int page, int size);
}
