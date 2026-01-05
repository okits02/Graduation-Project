package com.example.order_service.service.impl;

import com.example.order_service.dto.request.CheckValidVoucherRequest;
import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.response.*;
import com.example.order_service.enums.Status;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.repository.httpClient.ProductClient;
import com.example.order_service.repository.httpClient.PromotionClient;
import com.okits02.common_lib.dto.PageResponse;
import com.okits02.common_lib.exception.AppException;
import com.example.order_service.exceptions.OrderErrorCode;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.Orders;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.httpClient.UserClient;
import com.example.order_service.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PromotionClient promotionClient;
    private final UserClient userClient;
    private final ProductClient productClient;


    @Override
    public OrderResponse save(OrderCreationRequest request) {
        String userId = getUserId();
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        Orders orders = Orders.builder()
                .userId(userId)
                .addressId(request.getAddressId())
                .orderFee(request.getOrderFee())
                .orderDesc(request.getOrderDesc())
                .orderDate(LocalDateTime.now())
                .build();
        List<OrderItemResponse> items = createItem(request.getItems(), orders);
        orders.calculateTotalPrice();
        BigDecimal totalPrice = orders.getTotalPrice();
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getVoucher() != null && !request.getVoucher().isBlank()){
            CheckValidVoucherRequest checkValidVoucherRequest = CheckValidVoucherRequest.builder()
                    .today(Date.from(Instant.now()))
                    .totalAmount(orders.getTotalPrice().doubleValue())
                    .voucherCode(request.getVoucher())
                    .productId(request.getItems().stream().map(OrderItemRequest::getProductId).toList())
                    .categoryId(request.getCategoryId())
                    .build();
            var promotionResponse = promotionClient.checkValidPromotion(checkValidVoucherRequest, authHeader);
            if (promotionResponse != null
                    && promotionResponse.getResult() != null) {

                var promo = promotionResponse.getResult();

                discountAmount = calculateDiscount(totalPrice, promo);
            }
        }
        BigDecimal finalTotal =
                totalPrice.subtract(discountAmount)
                        .max(BigDecimal.ZERO);

        orders.setTotalPrice(finalTotal);
        Orders savedOrder = orderRepository.save(orders);
        applyVoucherToOrder(request.getVoucher(), savedOrder.getOrderId(), authHeader);
        return orderMapper.toOrderResponse(savedOrder);
    }

    private void applyVoucherToOrder(String voucherCode, String orderId, String authHeader){

        if (voucherCode == null || voucherCode.isBlank()) {
            return;
        }

        try {
            promotionClient.applyForOrder(
                    orderId,
                    voucherCode,
                    authHeader
            );
        } catch (FeignException.BadRequest e) {
            throw new AppException(
                    OrderErrorCode.VOUCHER_APPLY_FAILED);
        } catch (FeignException e) {
            throw new AppException(
                    OrderErrorCode.PROMOTION_SERVICE_UNAVAILABLE);
        }
    }
    private BigDecimal calculateDiscount(BigDecimal totalPrice, PromotionResponse promo) {
        BigDecimal discount = BigDecimal.ZERO;
        if(promo.getDiscountPercent() != null){
            discount = totalPrice.multiply(
                    BigDecimal.valueOf(promo.getDiscountPercent())
            ).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (promo.getMaxDiscountAmount() != null) {
                discount = discount.min(
                        BigDecimal.valueOf(promo.getMaxDiscountAmount())
                );
            }
        }
        if (promo.getFixedAmount() != null) {
            discount = BigDecimal.valueOf(promo.getFixedAmount())
                    .min(totalPrice);
        }
        return discount;
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
        if (status.equals(Status.CANCELLED) || status.equals(Status.FAILED)) {
            rollbackVoucher(orderId);
        }
        return orderMapper.toOrderResponse(orderRepository.save(orders));
    }

    private void rollbackVoucher(String orderId) {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            String authHeader = null;
            if (attrs != null && attrs.getRequest() != null) {
                authHeader = attrs.getRequest().getHeader("Authorization");
            }

            promotionClient.rollBackPromotion(orderId, authHeader);

        } catch (FeignException e) {
            log.error("Rollback voucher failed for orderId={}", orderId, e);
        } catch (Exception e) {
            log.error("Unexpected error when rollback voucher for orderId={}", orderId, e);
        }
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

    private List<OrderItemResponse> createItem(List<OrderItemRequest> itemRequests, Orders orders){
        if(itemRequests == null || itemRequests.isEmpty() || orders == null){
            return List.of();
        }
        List<OrderItemResponse> responses = new ArrayList<>();
        List<OrderItem> items = new ArrayList<>();
        for(OrderItemRequest item : itemRequests){
            var productResponse = productClient.getProductDetails(item.getSku());
            if (productResponse == null || productResponse.getCode() != 200) {
                throw new RuntimeException("Product not exists");
            }
            if(!item.getListPrice().equals(productResponse.getResult().getListPrice()) ||
                    !item.getSellPrice().equals(productResponse.getResult().getSellPrice())){
                throw new RuntimeException("Price for sku" + item.getSku() + "not valid");
            }
            items.add(OrderItem.builder()
                            .sku(item.getSku())
                            .quantity(item.getQuantity())
                            .sellPrice(item.getSellPrice())
                            .listPrice(item.getListPrice())
                            .addAt(LocalDateTime.now())
                            .orders(orders)
                    .build());
            responses.add(OrderItemResponse.builder()
                    .sku(item.getSku())
                    .productName(productResponse.getResult().getVariantName())
                    .thumbnailUrl(productResponse.getResult().getThumbnailUrl())
                    .quantity(item.getQuantity())
                    .sellPrice(item.getSellPrice())
                    .listPrice(item.getListPrice())
                    .addAt(LocalDateTime.now())
                    .build());
        }
        orders.setItems(items);
        return responses;
    }
}
