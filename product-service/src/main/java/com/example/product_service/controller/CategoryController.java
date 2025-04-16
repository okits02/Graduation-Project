package com.example.product_service.controller;

import com.cloudinary.Api;
import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    ApiResponse<CategoryResponse> createCate(@RequestBody @Valid CategoryRequest request)
    {
        try {
            return ApiResponse.<CategoryResponse>builder()
                    .code(200)
                    .result(categoryService.createCate(request))
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<CategoryResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @PutMapping("/update_cate")
    ApiResponse<CategoryResponse> updateCategory(@RequestBody @Valid CategoryRequest request)
    {
        try{
            return ApiResponse.<CategoryResponse>builder()
                    .code(200)
                    .result(categoryService.updateCate(request))
                    .build();
        }catch (AppException e)
        {
            return ApiResponse.<CategoryResponse>builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build();
        }
    }

    @GetMapping
    ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCate(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size)
    {
        if(page <= 0 || size <= 0)
        {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<Page<CategoryResponse>>builder()
                            .code(400)
                            .message("Page index must be non-negative and size must be greater than zero")
                            .build());
        }
        return ResponseEntity.ok(
                ApiResponse.<Page<CategoryResponse>>builder()
                        .code(200)
                        .result(categoryService.finAll(page, size))
                        .build());
    }

    @GetMapping("/{cate_id}")
    ApiResponse<CategoryResponse> getById(@PathVariable String categoryId)
    {
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .result(categoryService.findById(categoryId))
                .build();
    }

    @DeleteMapping("/{cate_id}")
    ApiResponse<CategoryResponse> deleteById(@PathVariable String categoryId)
    {
        categoryService.deleteCateById(categoryId);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("category delete successfully")
                .build();
    }
}

