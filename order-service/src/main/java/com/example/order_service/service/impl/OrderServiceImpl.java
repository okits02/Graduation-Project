package com.example.order_service.service.impl;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.order_service.exceptions.OrderErrorCode;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.Orders;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.httpClient.UserClient;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserClient userClient;


}
