package com.example.order_service.controller;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.RePaymentForOrder;
import com.example.order_service.dto.response.*;
import com.example.order_service.enums.Status;
import com.example.order_service.service.OrderService;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create")
    public ApiResponse<OrderResponse> create(
            @RequestBody OrderCreationRequest request
            ){
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("save order successfully!")
                .result(orderService.save(request))
                .build();
    }

    @PostMapping("/rePayment")
    public ApiResponse<OrderResponse> rePayment(
            @RequestBody RePaymentForOrder rePaymentForOrder
            ){
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("save order successfully!")
                .result(orderService.rePaymentForOrder(rePaymentForOrder))
                .build();
    }

    @PostMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> changeStatusOrder(
            @RequestParam("orderId") String orderId,
            @RequestParam("status") Status status
    ){
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .result(orderService.changeStatusOrder(orderId, status))
                .build();
    }


    @GetMapping("/get/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<OrderSummaryResponse>> getAllByStatus(
            @RequestParam("status") Status status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "0") Integer size
    ){
        return ApiResponse.<PageResponse<OrderSummaryResponse>>builder()
                .code(200)
                .message("get all order by status successfully!")
                .result(orderService.getAllByStatus(page - 1, size, status))
                .build();
    }

    @GetMapping("/get-by-userId")
    public ApiResponse<PageResponse<OrderSummaryResponse>> getByUserId(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "0") Integer size
    ){
        return ApiResponse.<PageResponse<OrderSummaryResponse>>builder()
                .code(200)
                .message("get order for user successfully!")
                .result(orderService.getByUserId(page, size))
                .build();
    }

    @GetMapping
    public ApiResponse<OrderResponse> getById(
            @RequestParam(value = "orderId") String orderId){
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("get order by id successfully")
                .result(orderService.getById(orderId))
                .build();
    }

    @PutMapping("/cancel")
    public ApiResponse<?> cancelOderById(
            @RequestParam(value = "orderId") String orderId
    ){
        return ApiResponse.<Object>builder()
                .code(200)
                .message("cancel order successfully")
                .result(orderService.cancelOrder(orderId))
                .build();
    }

    @GetMapping("/get-my-order")
    public ApiResponse<PageResponse<OrderSummaryResponse>> getByUserIdAndStatus(
            @RequestParam("status") Status status,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    ){
        return ApiResponse.<PageResponse<OrderSummaryResponse>>builder()
                .code(200)
                .message("get order for user successfully!")
                .result(orderService.getByUserIdAndStatus(page - 1, size, status))
                .build();
    }

    @GetMapping("/internal/getAmount")
    public ApiResponse<GetAmountResponse> getAmount(
            @RequestParam("orderId") String orderId
    ){
        return ApiResponse.<GetAmountResponse>builder()
                .code(200)
                .message("Get amount successfully!")
                .result(orderService.getAmount(orderId))
                .build();
    }

    @GetMapping("/internal/rating/check")
    public ApiResponse<CheckVerifiedPurchase> checkVerifiedPurchase(
            @RequestParam("userId") String userId,
            @RequestParam("productId") String productId
    ){
        return ApiResponse.<CheckVerifiedPurchase>builder()
                .result(orderService.checkVerifiedPurchase(userId, productId))
                .build();
    }

    @GetMapping("/internal/get-user-id")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<GetListUserIdResponse> getListUserIdResponse(){
        return ApiResponse.<GetListUserIdResponse>builder()
                .code(200)
                .message("get list userID successfully!")
                .result(orderService.getListUserId())
                .build();
    }

}
