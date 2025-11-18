package com.example.search_service.controller;

import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.*;
import com.example.search_service.viewmodel.dto.request.AdminSearchRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.search_service.viewmodel.dto.request.RemoveCategoryIdsRequest;
import com.example.search_service.viewmodel.dto.request.SearchRequest;
import com.okits02.common_lib.dto.PageResponse;
import lombok.RequiredArgsConstructor;

import com.example.search_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class ProductController {
    private final SearchService searchService;
    private final ProductService productService;

    @PostMapping("/catalog-search")
    public ResponseEntity<ProductGetListVM> searchAdvance(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestBody SearchRequest request
            )
    {
        return ResponseEntity.ok(searchService
                .searchProductAdvance(request.getKeyword(), page - 1, size,
                        request.getCategory(), request.getAttributes(), request.getMinPrice(), request.getMaxPrice(),
                        request.getSortType()));
    }

    @GetMapping("/search_suggest")
    public ResponseEntity<ProductNameGetListVm> productSearchAutoComplete(@RequestParam String keyword)
    {
        return ResponseEntity.ok(searchService.autoCompleteProductName(keyword));
    }

    @GetMapping("/internal/get-product/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailsVM>> getDetailsProduct(@PathVariable String productId){
        return ResponseEntity.ok(ApiResponse.<ProductDetailsVM>builder()
                .code(200)
                .result(productService.getDetailsProduct(productId))
                .build());
    }

    @PostMapping("/admin")
    public ApiResponse<PageResponse<ProductAdminGetVM>> searchAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestBody AdminSearchRequest request
    ){
        return ApiResponse.<PageResponse<ProductAdminGetVM>>builder()
                .code(200)
                .result(searchService.searchProductAdmin(page - 1, size, request))
                .build();
    }

    @PutMapping("/internal/categories/remove")
    public ResponseEntity<ApiResponse<Long>> removeCategoriesFromProducts(
            @RequestBody RemoveCategoryIdsRequest request
            ){
        long update = productService.removeCateInProduct(request.getCategoryIds());
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                        .code(200)
                        .result(update)
                        .message("Remove categories from products successfully")
                .build()
        );
    }
}
