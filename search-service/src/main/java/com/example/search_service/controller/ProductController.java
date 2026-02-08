package com.example.search_service.controller;

import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.*;
import com.example.search_service.viewmodel.dto.AutoCompletedResponse;
import com.example.search_service.viewmodel.dto.request.*;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import lombok.RequiredArgsConstructor;

import com.example.search_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
                        request.getSortType(), request.getOwnerId(), request.getOwnerType(), request.getFlashSale()));
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
            @RequestParam(value = "keyword") String keyword
    ) {
        return ApiResponse.<List<AutoCompletedResponse>>builder()
                .code(200)
                .message("get autocompleted full successfully!")
                .result(searchService.autocompleteFull(keyword))
                .build();
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

    @GetMapping("/product/sku")
    public ApiResponse<ProductGetVM> getBySku(@RequestParam("sku") String sku){
        return ApiResponse.<ProductGetVM>builder()
                .code(200)
                .message("Get product by sku successfully!")
                .result(searchService.getProductBySku(sku))
                .build();
    }

    @PostMapping("/product/suggest")
    public ApiResponse<ProductGetListVM> getProductSuggest(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestBody ProductSuggestRequest request
    ){
        return ApiResponse.<ProductGetListVM>builder()
                .code(200)
                .message("Get product suggest successfully!")
                .result(searchService.getListProductSuggest(request.getProductIds(),
                        request.getRecomentedType(), page - 1 , size))
                .build();
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

    @GetMapping("/internal/get-product/{productId}")
    public ResponseEntity<ApiResponse<ProductGetVM>> getDetailsProduct(@PathVariable String productId){
        return ResponseEntity.ok(ApiResponse.<ProductGetVM>builder()
                .code(200)
                .result(productService.getDetailsProduct(productId))
                .build());
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
    @GetMapping("/internal/product/list-sku")
    public ApiResponse<GetListSkuVM> getSkuByProductId(
            @RequestParam("productId") String productId
    ){
        return ApiResponse.<GetListSkuVM>builder()
                .code(200)
                .message("Get list sku successfully!")
                .result(searchService.getListSkuByProductId(productId))
                .build();
    }

    @PutMapping("/internal/sold")
    public ApiResponse<?> changeSold(
            @RequestParam(value = "sku") String sku,
            @RequestParam(value = "quantity") Integer quantity,
            @RequestParam(value = "transaction") String transaction
    ){
        productService.changeSold(sku, transaction, quantity);
        return ApiResponse.builder()
                .code(200)
                .build();
    }

    @PutMapping("/internal/stock")
    public ApiResponse<?> changStock(
            @RequestParam(value = "sku") String sku,
            @RequestParam(value = "isStock") Boolean isStock
    ) throws IOException {
        productService.changeStockRequest(sku, isStock);
        return ApiResponse.builder()
                .code(200)
                .build();
    }
}
