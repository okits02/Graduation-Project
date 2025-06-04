package com.example.product_service.controller;

import com.example.product_service.dto.request.CategoryRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.exceptions.AppException;
import com.example.product_service.model.Category;
import com.example.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Category> createCate(
            @RequestPart("file") MultipartFile multipartFile,
            @RequestPart("request") CategoryRequest request)
    {
        return ApiResponse.<Category>builder()
                .code(200)
                .result(categoryService.createCate(multipartFile, request))
                .build();
    }

    @PutMapping("/update_cate")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/getAll")
    ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCate(@RequestParam(defaultValue = "1") int page,
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
                        .result(categoryService.finAll(page - 1, size))
                        .build());
    }

    @GetMapping("/cate/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<CategoryResponse> getById(@PathVariable String categoryId)
    {
        log.info("id: {}", categoryId);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .result(categoryService.findById(categoryId))
                .build();
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<CategoryResponse> deleteById(@PathVariable String categoryId)
    {
        categoryService.deleteCateById(categoryId);
        return ApiResponse.<CategoryResponse>builder()
                .code(200)
                .message("category delete successfully")
                .build();
    }
}

