package com.example.search_service.controller;

import com.example.search_service.viewmodel.ProductGetListVM;
import com.example.search_service.viewmodel.ProductNameGetListVm;
import com.example.search_service.viewmodel.dto.request.SearchRequest;
import lombok.RequiredArgsConstructor;

import com.example.search_service.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class ProductController {
    private final SearchService searchService;

    @PostMapping("/catalog-search")
    public ResponseEntity<ProductGetListVM> searchAdvance(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestBody SearchRequest request
            )
    {
        return ResponseEntity.ok(searchService
                .searchProductAdvance(request.getKeyword(), page - 1, size, request.getCategory(),
                        request.getAttributes(), request.getMinPrice(), request.getMaxPrice(), request.getSortType()));
    }

    @GetMapping("search_suggest")
    public ResponseEntity<ProductNameGetListVm> productSearchAutoComplete(@RequestParam String keyword)
    {
        return ResponseEntity.ok(searchService.autoCompleteProductName(keyword));
    }
}
