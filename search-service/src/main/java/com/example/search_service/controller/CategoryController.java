package com.example.search_service.controller;

import com.example.search_service.service.CategoryService;
import com.example.search_service.service.SearchService;
import com.example.search_service.viewmodel.CategoryDetailsVM;
import com.example.search_service.viewmodel.CategoryGetListVM;
import com.example.search_service.viewmodel.dto.ApiResponse;
import com.example.search_service.viewmodel.dto.request.CategoryGetByListIdRequest;
import com.example.search_service.viewmodel.dto.request.SearchCateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search/category")
@RequiredArgsConstructor
public class CategoryController {
    private final SearchService searchService;
    private final CategoryService categoryService;
    
    @PostMapping("/admin")
    public ApiResponse<CategoryGetListVM> searchCategoryByName(
            @RequestBody SearchCateRequest request
    ){
        return ApiResponse.<CategoryGetListVM>builder()
                .code(200)
                .message("search category successfully")
                .result(searchService.autocompleteCategory(request.getName(), request.getSize(), request.getPage()))
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

    @GetMapping("/get-by-parentid/{categoryId}")
    public ApiResponse<CategoryDetailsVM> getByPrentId(@PathVariable String categoryId){
        return ApiResponse.<CategoryDetailsVM>builder()
                .code(200)
                .message("get category by id")
                .result(categoryService.getCategoryByParentId(categoryId))
                .build();
    }


}
