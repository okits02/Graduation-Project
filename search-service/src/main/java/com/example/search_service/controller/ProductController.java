package com.example.search_service.controller;

import com.example.search_service.constant.SortType;
import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.ProductGetListVM;
import com.example.search_service.viewmodel.ProductNameGetListVm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/search")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/catalog-search")
    public ResponseEntity<ProductGetListVM> searchAdvance(
            @RequestParam String keword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam String category,
            @RequestParam String attribute,
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            @RequestParam(defaultValue = "DEFAULT")SortType sortType
            )
    {
        return ResponseEntity.ok(productService
                .searchProductAdvance(keword, page, size, category, attribute, minPrice, maxPrice, sortType));
    }

    @GetMapping("search_suggest")
    public ResponseEntity<ProductNameGetListVm> productSearchAutoComplete(@RequestParam String keyword)
    {
        return ResponseEntity.ok(productService.autoCompleteProductName(keyword));
    }
}
