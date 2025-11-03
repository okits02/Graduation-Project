package com.okits02.inventory_service.controller;

import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.request.IsInStockRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.model.Inventory;
import com.okits02.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/create")
    public ApiResponse<InventoryResponse> save(
            @RequestBody InventoryRequest request
            ){
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Add product to inventory successfully!")
                .result(inventoryService.save(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<InventoryResponse> update(
            @RequestBody InventoryRequest request
    ){
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Update quantity of product in Inventory successfully!")
                .result(inventoryService.update(request))
                .build();
    }

    @DeleteMapping("/delete/{productId}")
    public ApiResponse<?> delete(
            @PathVariable String productId
    ){
        return ApiResponse.builder()
                .code(200)
                .message("Remove product in Inventory successfully!")
                .build();
    }

    @GetMapping("/get/{productId}")
    public ApiResponse<InventoryResponse> getByProductId(
            @PathVariable String productId
    ){
        return ApiResponse.<InventoryResponse>builder()
                .code(200)
                .message("Get product information at inventory success!")
                .result(inventoryService.getByProductId(productId))
                .build();
    }

    @PostMapping("/check-inStock")
    public ApiResponse<Boolean> checkInStock(
            @RequestBody IsInStockRequest request
    ){
        return ApiResponse.<Boolean>builder()
                .code(200)
                .result(inventoryService.checkIsStock(request))
                .build();
    }

    @PostMapping("/decrease-stock")
    public ApiResponse<?> decreaseStock(
            @RequestBody InventoryRequest request
    ){
        Inventory inventory = inventoryService.decreaseStock(request.getProductId(), request.getQuantity());
        return ApiResponse.builder()
                .code(200)
                .message("Decrease success!")
                .build();
    }

    @PostMapping("/increase-stock")
    public ApiResponse<?> increaseStock(
            @RequestBody InventoryRequest request
    ){
        Inventory inventory = inventoryService.increaseStock(request.getProductId(), request.getQuantity());
        return ApiResponse.builder()
                .code(200)
                .message("Decrease success!")
                .build();
    }

    @GetMapping("/get-all")
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
}
