package com.example.product_service.controller;

import com.example.product_service.dto.request.BrandCreationRequest;
import com.example.product_service.dto.response.BrandResponse;
import com.example.product_service.service.BrandService;
import com.okits02.common_lib.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/brand")
public class BrandController {
    private final BrandService brandService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> save(@RequestBody BrandCreationRequest request){
        return ApiResponse.<BrandResponse>builder()
                .code(200)
                .message("create brand successfully")
                .result(brandService.save(request))
                .build();
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BrandResponse> update(@RequestBody BrandCreationRequest request){
        return ApiResponse.<BrandResponse>builder()
                .code(200)
                .message("update promotion successfully!")
                .result(brandService.update(request))
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<BrandResponse>> getAll(){
        return ApiResponse.<List<BrandResponse>>builder()
                .code(200)
                .message("update promotion successfully!")
                .result(brandService.getList())
                .build();
    }

    @DeleteMapping("/delete/{brandName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable String brandName){
        brandService.delete(brandName);
        return ApiResponse.builder()
                .code(200)
                .message("delete brand successfully")
                .build();
    }

    @DeleteMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteListBrand(
            @RequestParam(value = "brands") List<String> brands
    ){
        brandService.deleteList(brands);
        return ApiResponse.builder()
                .code(200)
                .message("delete list brands successfully")
                .build();
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteAllProduct(
    ){
        brandService.deleteAll();
        return ApiResponse.builder()
                .code(200)
                .message("delete all brands successfully")
                .build();
    }
}
