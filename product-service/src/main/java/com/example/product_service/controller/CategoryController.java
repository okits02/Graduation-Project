package com.example.product_service.controller;

import com.example.product_service.dto.request.CategoryLevelValidateRequest;
import com.example.product_service.dto.response.CateListResponse;
import com.example.product_service.dto.response.CategoryLevelValidateResponse;
import com.example.product_service.kafka.CategoryEvent;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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
        Category response = categoryService.createCate(request);
        return ApiResponse.<Category>builder()
                .code(200)
                .result(response)
                .build();
    }

    @Operation(summary = "admin update category", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/update_cate")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<CategoryResponse> updateCategory(@RequestBody @Valid CategoryRequest request)
    {
        try{
            CategoryResponse response = categoryService.updateCate(request);
            return ApiResponse.<CategoryResponse>builder()
                    .code(200)
                    .result(response)
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
    ResponseEntity<ApiResponse<List<CateListResponse>>> getAllCate()
    {
        return ResponseEntity.ok(
                ApiResponse.<List<CateListResponse>>builder()
                        .code(200)
                        .result(categoryService.finAll())
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

    @DeleteMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteListCate(
            @RequestParam(value = "categories") List<String> categories
    ){
        categoryService.deleteCateByListId(categories);
        return ApiResponse.builder()
                .code(200)
                .message("delete list cate successfully")
                .build();
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteAllCate(
    ){
        categoryService.deleteAll();
        return ApiResponse.builder()
                .code(200)
                .message("delete alll cate successfully")
                .build();
    }

    @PostMapping("internal/validate-same-level")
    ApiResponse<CategoryLevelValidateResponse> CategoryValidateSameLevel(
            @RequestBody CategoryLevelValidateRequest request
    ){
        return ApiResponse.<CategoryLevelValidateResponse>builder()
                .code(200)
                .result(categoryService.validateSameLevel(request.getCategoryIds()))
                .build();
    }
}

