package com.example.promotion_service.controller;


import com.example.promotion_service.dto.request.PromotionCampaignRequest;
import com.example.promotion_service.dto.response.ApiResponse;
import com.example.promotion_service.dto.response.PromotionCampaignResponse;
import com.example.promotion_service.services.PromotionCampaignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
@Slf4j
public class PromotionCampaignController {
    private final PromotionCampaignService promotionCampaignService;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PromotionCampaignResponse> save(
            @RequestBody PromotionCampaignRequest request
    ){
        return ApiResponse.<PromotionCampaignResponse>builder()
                .code(200)
                .message("create campaign successfully")
                .Result(promotionCampaignService.save(request))
                .build();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PromotionCampaignResponse> update(
            @RequestBody PromotionCampaignRequest request
    ){
        return ApiResponse.<PromotionCampaignResponse>builder()
                .code(200)
                .message("update campaign successfully")
                .Result(promotionCampaignService.update(request))
                .build();
    }

    @DeleteMapping("/id")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteById(
            @RequestParam String id
    ){
        promotionCampaignService.delete(id);
        return ApiResponse.builder()
                .code(200)
                .message("delete campaign successfully")
                .build();
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> deleteAll(
    ){
        promotionCampaignService.deleteAll();
        return ApiResponse.builder()
                .code(200)
                .message("delete campaign successfully")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PromotionCampaignResponse>> getAll(){
        return ApiResponse.<List<PromotionCampaignResponse>>builder()
                .code(200)
                .message("get campaign successfully")
                .Result(promotionCampaignService.getAll())
                .build();
    }

}
