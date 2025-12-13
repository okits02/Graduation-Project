package com.example.order_service.service.impl;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.response.GetAmountResponse;
import com.example.order_service.dto.response.ItemSummaryResponse;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.dto.response.OrderSummaryResponse;
import com.example.order_service.enums.Status;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.repository.OrderItemRepository;
import com.okits02.common_lib.dto.PageResponse;
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
import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final OrderItemMapper orderItemMapper;
    private final UserClient userClient;


    @Override
    public OrderResponse save(OrderCreationRequest request) {
        String userId = getUserId();
        Orders orders = Orders.builder()
                .userId(userId)
                .addressId(request.getAddressId())
                .orderFee(request.getOrderFee())
                .orderDesc(request.getOrderDesc())
                .orderDate(LocalDateTime.now())
                .build();
        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> {
                    OrderItem item = orderItemMapper.toOrderItem(itemReq);
                    item.setOrders(orders);
                    return item;
                }).toList();
        orders.setItems(items);
        orders.calculateTotalPrice();
        return orderMapper.toOrderResponse(orderRepository.save(orders));
    }

    @Override
    public PageResponse<OrderSummaryResponse> getByUserId(int page, int size) {
        String userId = getUserId();
        Pageable pageable = PageRequest.of(page, size);
        var pageData = orderRepository.findAllByUserId(userId, pageable);
        List<OrderSummaryResponse> orderSummaries = pageData.map(order -> new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(i -> new ItemSummaryResponse(
                                i.getProductName(),
                                i.getThumbnailUrl(),
                                i.getSellPrice(),
                                i.getListPrice(),
                                i.getQuantity()
                        ))
                        .toList()
        )).getContent();
        return PageResponse.<OrderSummaryResponse>builder()
                .currentPage(page)
                .totalElements(pageData.getTotalElements())
                .pageSize(pageData.getSize())
                .data(orderSummaries)
                .build();
    }

    @Override
    public PageResponse<OrderSummaryResponse> getByUserIdAndStatus(int page, int size, Status status) {
        String userId = getUserId();
        Pageable pageable = PageRequest.of(page, size);
        var pageData = orderRepository.findAllByUserIdAndStatus(userId, status, pageable);
        List<OrderSummaryResponse> orderSummaries = pageData.map(order -> new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(i -> new ItemSummaryResponse(
                                i.getProductName(),
                                i.getThumbnailUrl(),
                                i.getSellPrice(),
                                i.getListPrice(),
                                i.getQuantity()
                        ))
                        .toList()
        )).getContent();
        return PageResponse.<OrderSummaryResponse>builder()
                .currentPage(page)
                .totalElements(pageData.getTotalElements())
                .pageSize(pageData.getSize())
                .data(orderSummaries)
                .build();
    }

    @Override
    public void cancelOrder(String orderId) {
        String userId = getUserId();
        Orders orders = orderRepository.findById(orderId).orElseThrow(
                () -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        orders.setOrderStatus(Status.CANCELLED);
        orderRepository.save(orders);
    }

    @Override
    public OrderResponse changeStatusOrder(String orderId, Status status) {
        Orders orders = orderRepository.findById(orderId).orElseThrow(
                () -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        orders.setOrderStatus(status);
        return orderMapper.toOrderResponse(orderRepository.save(orders));
    }

    @Override
    public void changStatusOrderForPayment(String paymentId, String orderId, Status status) {
        Orders orders = orderRepository.findById(orderId).orElseThrow(()
                -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        orders.setOrderStatus(status);
        orders.setPaymentId(paymentId);
        orderRepository.save(orders);
    }

    @Override
    public GetAmountResponse getAmount(String orderId) {
        Orders orders = orderRepository.findById(orderId).orElseThrow(() ->
                new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        return GetAmountResponse.builder()
                .amount(orders.getTotalPrice())
                .build();
    }

    @Override
    public PageResponse<OrderSummaryResponse> getAllByStatus(int page, int size, Status status) {
        Pageable pageable = PageRequest.of(page, size);
        var pageData = orderRepository.findAllByStatus(status, pageable);
        List<OrderSummaryResponse> orderSummaries = pageData.map(order -> new OrderSummaryResponse(
                order.getOrderId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getItems().stream()
                        .map(i -> new ItemSummaryResponse(
                                i.getProductName(),
                                i.getThumbnailUrl(),
                                i.getSellPrice(),
                                i.getListPrice(),
                                i.getQuantity()
                        ))
                        .toList()
        )).getContent();
        return PageResponse.<OrderSummaryResponse>builder()
                .currentPage(page)
                .totalElements(pageData.getTotalElements())
                .pageSize(pageData.getSize())
                .data(orderSummaries)
                .build();
    }

    @Override
    public OrderResponse getById(String orderId) {
        Orders orders = orderRepository.findById(orderId).orElseThrow(()
                -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
        return orderMapper.toOrderResponse(orders);
    }

    private String getUserId(){
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        var apiResponse = userClient.getUserId(authHeader);
        return apiResponse.getResult().getUserId();
    }
}
