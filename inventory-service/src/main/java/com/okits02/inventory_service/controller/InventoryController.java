package com.okits02.inventory_service.controller;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.request.InventoryAdjustmentRequest;
import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.dto.response.InventoryTransactionResponse;
import com.okits02.inventory_service.model.Inventory;
import com.okits02.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;


    @GetMapping("/get")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InventoryResponse> getByProductIdAndSku(
            @RequestParam String sku
    ){
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Get product information at inventory success!")
                .result(inventoryService.getByProductIdAndSku(sku))
                .build();
    }

    @PostMapping("/internal/check-inStock")
    public ApiResponse<Boolean> checkInStock(
            @RequestBody IsInStockRequest request
    ){
        return ApiResponse.<Boolean>builder()
                .code(200)
                .result(inventoryService.checkIsStock(request))
                .build();
    }

    @PostMapping("/internal/decrease-stock")
    public ApiResponse<?> decreaseStock(
            @RequestBody InventoryAdjustmentRequest request
    ){
        Inventory inventory = inventoryService.decreaseStock(request.getSku(),
                request.getQuantity(), request.getOrderId());
        return ApiResponse.builder()
                .code(200)
                .message("Decrease success!")
                .build();
    }

    @PostMapping("/internal/increase-stock")
    public ApiResponse<?> increaseStock(
            @RequestBody InventoryAdjustmentRequest request
    ){
        Inventory inventory = inventoryService.increaseStock(request.getSku(),
                request.getQuantity(), request.getOrderId());
        return ApiResponse.builder()
                .code(200)
                .message("increase success!")
                .build();
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<?>> getAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<?>>builder()
                .code(200)
                .message("get all inventory successfully!")
                .result(inventoryService.getAll(page - 1, size))
                .build();
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<InventoryTransactionResponse> getTransactionHistory(
            @RequestParam String sku,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inventoryService.getTransactionHistory(sku, page-1, size);
    }

    @GetMapping("/internal/quantity")
    public ApiResponse<List<Inventory>> getListQuantity(
            @RequestParam(value = "skus") List<String> skus
    ){
        return ApiResponse.<List<Inventory>>builder()
                .code(200)
                .result(inventoryService.getQuantityByListSkus(skus))
                .build();
    }

}
