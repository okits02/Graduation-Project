package com.example.order_service.service.impl;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.Orders;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.service.OrderItemService;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemService orderItemService;

    @Override
    public OrderResponse save(OrderCreationRequest request) {
        Orders orders = Orders.builder()
                .orderDate(LocalDateTime.now())
                .orderFee(request.getOrderFee())
                .orderDesc(request.getOrderDesc())
                .isCheckout(false)
                .build();
        if(!request.getItems().isEmpty()){
            List<OrderItem> itemList = new ArrayList<>();
            for(OrderItemRequest item : request.getItems()){
                OrderItem orderItem = orderItemService.save(item.getProductId(), item.getQuantity(), orders);
                itemList.add(orderItem);
            }
            orders.setItems(itemList);
        }
        return orderMapper.toOrderResponse(orderRepository.save(orders));

    }
}
