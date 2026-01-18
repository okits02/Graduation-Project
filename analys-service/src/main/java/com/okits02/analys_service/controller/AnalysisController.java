package com.okits02.analys_service.controller;

import com.okits02.analys_service.viewmodel.OrderStatusChartPoint;
import com.okits02.analys_service.viewmodel.StockProductTable;
import com.okits02.analys_service.viewmodel.dto.request.ChartQueryRequest;
import com.okits02.analys_service.service.AnalysisService;
import com.okits02.analys_service.viewmodel.RevenueStockChartPoint;
import com.okits02.common_lib.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;

    @PostMapping("/revenue")
    public ApiResponse<List<RevenueStockChartPoint>> getRevenueStockChartPoint(
            @RequestBody ChartQueryRequest request
            ){
        return ApiResponse.<List<RevenueStockChartPoint>>builder()
                .code(200)
                .result(analysisService.statisticDashboard(request))
                .build();
    }

    @PostMapping("/statitis")
    public ApiResponse<List<OrderStatusChartPoint>> getOrderStatusChartPoint(
            @RequestBody ChartQueryRequest request
    ){
        return ApiResponse.<List<OrderStatusChartPoint>>builder()
                .code(200)
                .result(analysisService.getOrderStatusCharPoint(request))
                .build();
    }

    @GetMapping("/bestSelling")
    public ApiResponse<List<StockProductTable>> get10BestSelling(){
        return ApiResponse.<List<StockProductTable>>builder()
                .code(200)
                .result(analysisService.getTop10BestSellingProducts())
                .build();
    }

    @GetMapping("/slowSelling")
    public ApiResponse<List<StockProductTable>> get10SlowSelling(){
        return ApiResponse.<List<StockProductTable>>builder()
                .code(200)
                .result(analysisService.getTop10SlowSellingProducts())
                .build();
    }

}
