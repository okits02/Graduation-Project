package com.example.product_service.controller;

import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductSearchRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.model.Products;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    ProductRepository productRepository;

    @PostMapping
    ApiResponse<Products> createProduct(@RequestBody @Valid ProductRequest request)
    {
        try{
            return ApiResponse.<Products>builder()
                    .code(200)
                    .result(productService.createProduct(request))
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<Products>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @GetMapping("/search_product")
    ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProduct(@RequestBody @Valid ProductSearchRequest request){
        try{
            return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                    .code(200)
                    .result(productService.searchProducts(request))
                    .build());
        } catch (AppException e)
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Page<ProductResponse>>builder()
                            .code(e.getErrorCode().getCode())
                            .message(e.getMessage())
                            .build());
        }
    }

    @PutMapping("/update")
    ApiResponse<ProductResponse> updateProduct(@RequestBody @Valid ProductRequest request)
    {
        try{
            return ApiResponse.<ProductResponse>builder()
                    .code(200)
                    .result(productService.updateProduct(request))
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<ProductResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @GetMapping
    ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProduct(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size)
    {
        if(page <= 0 || size <= 0)
        {
            ResponseEntity.badRequest().body(ApiResponse.<Page<ProductResponse>>builder()
                    .code(400)
                    .message("Page index must be non-negative and size must be greater than zero")
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<Page<ProductResponse>>builder()
                .code(200)
                .result(productService.getAll(page, size))
                .build());
    }

    @GetMapping("/{product_id}")
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

    @DeleteMapping("/{product_id}")
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
}
