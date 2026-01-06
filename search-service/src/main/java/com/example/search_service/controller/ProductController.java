package com.example.search_service.controller;

import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.*;
import com.example.search_service.viewmodel.dto.AutoCompletedResponse;
import com.example.search_service.viewmodel.dto.request.AdminSearchRequest;
import com.example.search_service.viewmodel.dto.request.ProductGetByListIdRequest;
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
                .searchProductAdvance(request.getKeyword(), page - 1, size, request.getBrandName(),
                        request.getCategory(), request.getAttributes(), request.getMinPrice(), request.getMaxPrice(),
                        request.getSortType()));
    }

    @GetMapping("/autocomplete/quick")
    public ApiResponse<List<AutoCompletedResponse>> autocompleteQuick(
            @RequestParam("q") String keyword,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        return ApiResponse.<List<AutoCompletedResponse>>builder()
                .code(200)
                .message("get autocompleted quick")
                .result(searchService.autoCompletedProductQuick(keyword, limit))
                .build();
    }


    @GetMapping("/autocomplete/full")
    public ApiResponse<List<AutoCompletedResponse>> autocompleteFull(
            @RequestParam("q") String keyword
    ) {
        return ApiResponse.<List<AutoCompletedResponse>>builder()
                .code(200)
                .message("get autocompleted full successfully!")
                .result(searchService.autocompleteFull(keyword))
                .build();
    }

    @GetMapping("/internal/get-product/{productId}")
    public ResponseEntity<ApiResponse<ProductGetVM>> getDetailsProduct(@PathVariable String productId){
        return ResponseEntity.ok(ApiResponse.<ProductGetVM>builder()
                .code(200)
                .result(productService.getDetailsProduct(productId))
                .build());
    }

    @PostMapping("/admin")
    public ApiResponse<ProductGetListVM> searchAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestBody AdminSearchRequest request
    ){
        return ApiResponse.<ProductGetListVM>builder()
                .code(200)
                .result(searchService.searchProductAdmin(page - 1, size, request))
                .build();
    }

    @PostMapping("/admin/get-by-list-id")
    public ApiResponse<ProductGetListVM> getByListIds(
            @RequestBody ProductGetByListIdRequest request
            ){
        return ApiResponse.<ProductGetListVM>builder()
                .code(200)
                .message("get product by list ids successfully")
                .result(productService.getByListIds(request.getProductIds(), request.getPage() - 1, request.getSize()))
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

    @GetMapping("/product")
    public ApiResponse<ProductGetVM> getById(
            @RequestParam("productId") String productId
    ){
        return ApiResponse.<ProductGetVM>builder()
                .code(200)
                .message("Get product by id successfully!")
                .result(searchService.getProductById(productId))
                .build();
    }

    @GetMapping("/internal/product/sku")
    public ApiResponse<List<ProductSkuVM>> getBySku(
            @RequestParam("skus") List<String> skus
    ){
         return ApiResponse.<List<ProductSkuVM>>builder()
                 .code(200)
                 .message("Get product variant by sku!")
                 .result(searchService.getProductBySku(skus))
                 .build();
    }
}
