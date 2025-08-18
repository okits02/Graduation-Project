package com.example.order_service.service;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderUpdateRequest;
import com.example.order_service.dto.response.OrderResponse;
import reactor.core.publisher.Mono;

public interface OrderService {
    public OrderResponse save(OrderCreationRequest request);
}
