package com.example.product_service.controller;

import com.okits02.common_lib.dto.PageResponse;
import com.example.product_service.dto.request.CategoryRequest;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.product_service.dto.response.CategoryResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.product_service.model.Category;
import com.example.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    @Operation(summary = "admin create category", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Category> createCate(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<Category>builder()
                .code(200)
                .result(categoryService.createCate(request))
                .build();
    }

    @Operation(summary = "admin update category", security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "get all category")
    @GetMapping("/getAll")
    ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getAllCate(@RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "10") int size)
    {
        if(page <= 0 || size <= 0)
        {
            return ResponseEntity.badRequest().body(
                    ApiResponse.<PageResponse<CategoryResponse>>builder()
                            .code(400)
                            .message("Page index must be non-negative and size must be greater than zero")
                            .build());
        }
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<CategoryResponse>>builder()
                        .code(200)
                        .result(categoryService.finAll(page - 1, size))
                        .build());
    }

    @Operation(summary = "admin get category by id",
            security = @SecurityRequirement(name = "bearerAuth"))
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

    @Operation(summary = "admin delete category",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/delete/{categoryId}")
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

