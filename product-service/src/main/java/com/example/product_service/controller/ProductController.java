package com.example.product_service.controller;

import com.example.product_service.dto.response.ProductVariantsResponse;
import com.example.product_service.service.ProductVariantsService;
import com.okits02.common_lib.dto.PageResponse;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductUpdateRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.product_service.helper.ProductMappingHelper;
import com.example.product_service.model.Products;
import com.example.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    ProductVariantsService productVariantsService;
    ProductMappingHelper productMappingHelper;

    @Operation(summary = "admin create product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductResponse> createProduct(
            @RequestBody ProductRequest request)
    {
            Products product = productService.createProduct(request);
            return ApiResponse.<ProductResponse>builder()
                    .code(200)
                    .result(productMappingHelper.map(product))
                    .build();
    }

    @Operation(summary = "admin update product", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductResponse> updateProduct(@RequestBody @Valid ProductUpdateRequest request)
    {
        try{
            Products product = productService.updateProduct(request);
            ProductResponse productResponse = productMappingHelper.map(product);
            return ApiResponse.<ProductResponse>builder()
                    .code(200)
                    .result(productResponse)
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<ProductResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "get all product")
    @GetMapping("/getAll")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProduct(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size)
    {
        if(page <= 0 || size <= 0)
        {
            ResponseEntity.badRequest().body(ApiResponse.<Page<ProductResponse>>builder()
                    .code(400)
                    .message("Page index must be non-negative and size must be greater than zero")
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<PageResponse<ProductResponse>>builder()
                .code(200)
                .result(productService.getAll(page - 1, size))
                .build());
    }

    @Operation(summary = "get product by id")
    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductResponse> getById(@PathVariable String productId)
    {
        try{
            return ApiResponse.<ProductResponse>builder()
                    .code(200)
                    .result(productService.getById(productId))
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<ProductResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        } 
    }

    @Operation(summary = "admin delete product", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteProduct(@PathVariable String productId)
    {
        try {
            productService.DeleteProduct(productId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("Product delete successfully")
                    .build());
        }catch (AppException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteListProduct(
            @RequestParam(value = "productIds") List<String> productIds
    ){
        productService.DeleteListProduct(productIds);
        return ApiResponse.builder()
                .code(200)
                .message("delete list product successfully")
                .build();
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteAllProduct(
    ){
        productService.DeleteAll();
        return ApiResponse.builder()
                .code(200)
                .message("delete all product successfully")
                .build();
    }
}
