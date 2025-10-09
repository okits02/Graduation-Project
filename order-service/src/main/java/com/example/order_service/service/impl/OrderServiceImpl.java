package com.example.order_service.service.impl;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.exceptions.AppException;
import com.example.order_service.exceptions.ErrorCode;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.Orders;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.httpClient.UserClient;
import com.example.order_service.service.OrderItemService;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final UserClient userClient;

    @Override
    public OrderResponse save(OrderCreationRequest request) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert servletRequestAttributes != null;
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var response = userClient.getUserId(authHeader);
        String userId = null;
        if(response.getBody().getCode() != 200){
            throw new AppException(ErrorCode.valueOf(response.getBody()));
        }
        Orders orders = Orders.builder()
                .orderDate(LocalDateTime.now())
                .orderFee(request.getOrderFee())
                .orderDesc(request.getOrderDesc())
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
