package com.okits02.inventory_service.controller;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.inventory_service.dto.request.GetHistoryRequest;
import com.okits02.inventory_service.dto.request.StockInCreationRequest;
import com.okits02.inventory_service.dto.response.StockInResponse;
import com.okits02.inventory_service.service.StockInService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/stock-in")
@RequiredArgsConstructor
public class StockInController {
    private final StockInService stockInService;

    @PostMapping("/create")
    public ApiResponse<StockInResponse> save(@RequestBody StockInCreationRequest request){
        return ApiResponse.<StockInResponse>builder()
                .code(200)
                .message("create stock receipt successfully!")
                .result(stockInService.save(request))
                .build();
    }

    @GetMapping("/get-by-referenceCode/{referenceCode}")
    public ApiResponse<StockInResponse> getByReferenceCode(@PathVariable String referenceCode){
        return ApiResponse.<StockInResponse>builder()
                .code(200)
                .message("get stock receipt by reference code successfully!")
                .result(stockInService.getByReferenceCode(referenceCode))
                .build();
    }

    @GetMapping("/get-history")
    public ApiResponse<PageResponse<StockInResponse>> getHistory(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
            ){
        return ApiResponse.<PageResponse<StockInResponse>>builder()
                .code(200)
                .message("get all history for stock receipt successfully!")
                .result(stockInService.getAllHistory(page, size, start, end))
                .build();
    }

    @DeleteMapping("/delete/{referenceCode}")
    public ApiResponse<?> delete(@PathVariable String referenceCode){
        stockInService.deleteStockIn(referenceCode);
        return ApiResponse.<StockInResponse>builder()
                .code(200)
                .message("delete stock receipt successfully!")
                .build();
    }
}
