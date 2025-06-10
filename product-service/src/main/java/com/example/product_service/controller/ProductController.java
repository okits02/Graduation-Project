package com.example.product_service.controller;

import com.example.product_service.dto.PageResponse;
import com.example.product_service.dto.request.ProductRequest;
import com.example.product_service.dto.request.ProductUpdateRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.exceptions.ErrorCode;
import com.example.product_service.kafka.CreateProductEvent;
import com.example.product_service.model.Category;
import com.example.product_service.model.Products;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.CategoryService;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductService productService;
    CategoryService categoryService;
    CategoryRepository categoryRepository;
    KafkaTemplate<String, Object> kafkaTemplate;

    @Operation(summary = "admin create product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Products> createProduct(
            @RequestBody ProductRequest request)
    {
        try{
            Products product = productService.createProduct(request);
            CreateProductEvent createProductEvent = createEventProduct(product);
            kafkaTemplate.send("create-product", createProductEvent).whenComplete(
                    (result, ex) -> {
                        if(ex != null)
                        {
                            System.err.println("Failed to send message" + ex.getMessage());
                        }else
                        {
                            System.err.println("send message successfully" + result.getProducerRecord());
                        }
                    });
            return ApiResponse.<Products>builder()
                    .code(200)
                    .result(product)
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<Products>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "admin update product", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductResponse> updateProduct(@RequestBody @Valid ProductUpdateRequest request)
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

    @Operation(summary = "get all product")
    @GetMapping("/getAll")
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

    @Operation(summary = "admin delete product", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{product_id}")
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

    private CreateProductEvent createEventProduct(Products product)
    {
        String currentCateId = product.getCategoryId();
        List<String> categoryId = categoryService.getCategoryHierarchy(currentCateId);
        List<String> categories = new ArrayList<>();
        for(String cateId : categoryId){
            Category category = categoryRepository.findById(cateId).orElseThrow(()
                    -> new AppException(ErrorCode.CATE_NOT_EXISTS));
            categories.add(category.getName());
        }
        CreateProductEvent createProductEvent = CreateProductEvent.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .listPrice(product.getListPrice())
                .sellPrice(product.getSellPrice())
                .quantity(product.getQuantity())
                .avgRating(product.getAvgRating())
                .sold(product.getSold())
                .discount(product.getDiscount())
                .imageList(product.getImageList())
                .categories(categories)
                .specifications(product.getSpecifications())
                .createAt(product.getCreateAt())
                .updateAt(product.getUpdateAt())
                .build();
        return  createProductEvent;
    }
}
