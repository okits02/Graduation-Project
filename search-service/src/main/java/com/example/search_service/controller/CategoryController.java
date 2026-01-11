package com.example.search_service.controller;

import com.example.search_service.service.CategoryService;
import com.example.search_service.service.SearchService;
import com.example.search_service.viewmodel.CategoryDetailsVM;
import com.example.search_service.viewmodel.CategoryGetListVM;
import com.example.search_service.viewmodel.dto.response.ApiResponse;
import com.example.search_service.viewmodel.dto.AutoCompletedResponse;
import com.example.search_service.viewmodel.dto.request.CategoryGetByListIdRequest;
import com.example.search_service.viewmodel.dto.request.SearchCateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search/category")
@RequiredArgsConstructor
public class CategoryController {
    private final SearchService searchService;
    private final CategoryService categoryService;
    
    @PostMapping("/admin")
    public ApiResponse<List<AutoCompletedResponse>> searchCategoryByName(
            @RequestBody SearchCateRequest request
    ){
        return ApiResponse.<List<AutoCompletedResponse>>builder()
                .code(200)
                .message("search category successfully")
                .result(searchService.autocompleteCategory(request.getName(), request.getLimit()))
                .build();
    }

    @PostMapping("/get-by-list-id")
    public ApiResponse<CategoryGetListVM> getByListIds(
            @RequestBody CategoryGetByListIdRequest request
    ){
        return ApiResponse.<CategoryGetListVM>builder()
                .code(200)
                .message("search category successfully")
                .result(categoryService.getCategoryByListIds(request.getCategoryIds()))
                .build();
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryDetailsVM> getByPrentId(@PathVariable String categoryId){
        return ApiResponse.<CategoryDetailsVM>builder()
                .code(200)
                .message("get category by id")
                .result(categoryService.getCategoryTreeById(categoryId))
                .build();
    }


}
