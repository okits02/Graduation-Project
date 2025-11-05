package com.example.order_service.service;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderUpdateRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.OrderSummaryResponse;
import com.example.order_service.enums.Status;
import com.okits02.common_lib.dto.PageResponse;
import reactor.core.publisher.Mono;

public interface OrderService {
    public OrderResponse save(OrderCreationRequest request);
    public PageResponse<OrderSummaryResponse> getByUserId(int page, int size);
    public PageResponse<OrderSummaryResponse> getByUserIdAndStatus(int page, int size, Status status);
    public void cancelOrder(String orderId);
    public OrderResponse changeStatusOrder(String orderId, Status status);
    public PageResponse<OrderSummaryResponse> getAllByStatus(int page, int size, Status status);
    public OrderResponse getById(String orderId);
}
