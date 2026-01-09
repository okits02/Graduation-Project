    package com.example.order_service.service.impl;

    import com.example.order_service.dto.ProductSkuVM;
    import com.example.order_service.dto.request.*;
    import com.example.order_service.dto.response.*;
    import com.example.order_service.enums.Status;
    import com.example.order_service.kafka.OrderAnalysisEvent;
    import com.example.order_service.kafka.OrderItemEvent;
    import com.example.order_service.mapper.OrderItemMapper;
    import com.example.order_service.repository.httpClient.InventoryClient;
    import com.example.order_service.repository.httpClient.SearchClient;
    import com.example.order_service.repository.httpClient.PromotionClient;
    import com.okits02.common_lib.dto.ApiResponse;
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
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.kafka.core.KafkaTemplate;
    import org.springframework.stereotype.Service;
    import org.springframework.web.context.request.RequestContextHolder;
    import org.springframework.web.context.request.ServletRequestAttributes;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.time.Instant;
    import java.time.LocalDateTime;
    import java.util.*;
    import java.util.function.Function;
    import java.util.stream.Collectors;
    import java.util.stream.Stream;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class OrderServiceImpl implements OrderService {
        private final OrderRepository orderRepository;
        private final OrderMapper orderMapper;
        private final PromotionClient promotionClient;
        private final UserClient userClient;
        private final SearchClient searchClient;
        private final InventoryClient inventoryClient;
        private final KafkaTemplate<String, Object> kafkaTemplate;


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
            List<String> skus = request.getItems().stream()
                    .map(OrderItemRequest::getSku)
                    .distinct()
                    .toList();

            var productResponse = searchClient.getProductDetails(skus);
            if (productResponse == null || productResponse.getCode() != 200 || productResponse.getResult() == null) {
                throw new RuntimeException("Cannot fetch product info");
            }

            Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                    .collect(Collectors.toMap(ProductSkuVM::getSku, Function.identity()));
            List<OrderItemResponse> itemResponses = buildOrderItemAndResponse(request.getItems(), orders, productMap);

            orders.calculateTotalPrice();
            BigDecimal totalPrice = orders.getTotalPrice();
            BigDecimal discountAmount = BigDecimal.ZERO;
            List<String> productIds = productMap.values().stream()
                    .map(ProductSkuVM::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            List<String> categoryIds = productMap.values().stream()
                    .flatMap(p -> p.getCategoriesId() == null ? Stream.empty() : p.getCategoriesId().stream())
                    .distinct()
                    .toList();
            if (request.getVoucher() != null && !request.getVoucher().isBlank()){
                CheckValidVoucherRequest checkValidVoucherRequest = CheckValidVoucherRequest.builder()
                        .today(Date.from(Instant.now()))
                        .totalAmount(orders.getTotalPrice().doubleValue())
                        .voucherCode(request.getVoucher())
                        .productId(productIds)
                        .categoryId(categoryIds)
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
            orders.setOrderStatus(Status.PENDING);
            Orders savedOrder = orderRepository.save(orders);
            applyVoucherToOrder(request.getVoucher(), savedOrder.getOrderId(), authHeader);
            var response = orderMapper.toOrderResponse(savedOrder);
            response.setItems(itemResponses);
            return response;
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
            List<Orders> orders = pageData.getContent();

            if(orders == null){
                return PageResponse.<OrderSummaryResponse>builder()
                        .currentPage(page)
                        .totalElements(0L)
                        .pageSize(size)
                        .data(List.of())
                        .build();
            }

            List<String> skus = orders.stream()
                    .flatMap(o -> o.getItems().stream())
                    .map(OrderItem::getSku)
                    .distinct()
                    .toList();

            var productResponse = searchClient.getProductDetails(skus);
            if (productResponse == null || productResponse.getCode() != 200) {
                throw new RuntimeException("Cannot fetch product info");
            }

            Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductSkuVM::getSku,
                            Function.identity()
                    ));

            List<OrderSummaryResponse> orderSummaries = orders.stream()
                    .map(order -> new OrderSummaryResponse(
                            order.getOrderId(),
                            order.getOrderDate(),
                            order.getTotalPrice(),
                            order.getItems().stream()
                                    .map(i -> {
                                        ProductSkuVM product = productMap.get(i.getSku());
                                        return new ItemSummaryResponse(
                                                product != null ? product.getVariantName() : null,
                                                product != null ? product.getThumbnailUrl() : null,
                                                i.getSellPrice(),
                                                i.getListPrice(),
                                                i.getQuantity()
                                        );
                                    })
                                    .toList()
                    )).toList();

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
            List<Orders> orders = pageData.getContent();

            if(orders == null){
                return PageResponse.<OrderSummaryResponse>builder()
                        .currentPage(page)
                        .totalElements(0L)
                        .pageSize(size)
                        .data(List.of())
                        .build();
            }

            List<String> skus = orders.stream()
                    .flatMap(o -> o.getItems().stream())
                    .map(OrderItem::getSku)
                    .toList();

            var productResponse = searchClient.getProductDetails(skus);
            if (productResponse == null || productResponse.getCode() != 200) {
                throw new RuntimeException("Cannot fetch product info");
            }

            Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductSkuVM::getSku,
                            Function.identity()
                    ));

            List<OrderSummaryResponse> orderSummaries = orders.stream()
                    .map(order -> new OrderSummaryResponse(
                            order.getOrderId(),
                            order.getOrderDate(),
                            order.getTotalPrice(),
                            order.getItems().stream()
                                    .map(i -> {
                                        ProductSkuVM product = productMap.get(i.getSku());
                                        return new ItemSummaryResponse(
                                                product != null ? product.getVariantName() : null,
                                                product != null ? product.getThumbnailUrl() : null,
                                                i.getSellPrice(),
                                                i.getListPrice(),
                                                i.getQuantity()
                                        );
                                    })
                                    .toList()
                    ))
                    .toList();

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
            sendKafkaEventToAnalysis(orders);
            orderRepository.save(orders);
        }

        @Override
        public OrderResponse changeStatusOrder(String orderId, Status status) {
            Orders orders = orderRepository.findById(orderId).orElseThrow(
                    () -> new AppException(OrderErrorCode.ORDER_NOT_EXISTS));
            orders.setOrderStatus(status);
            if (status.equals(Status.CANCELLED) || status.equals(Status.RETURNED)) {
                rollbackVoucher(orderId);
                increaseInventory(orders);
                sendKafkaEventToAnalysis(orders);
            }
            if(status.equals(Status.COMPLETED)){
                sendKafkaEventToAnalysis(orders);
            }
            orderRepository.save(orders);
            List<String> skus = orders.getItems()
                    .stream()
                    .map(OrderItem::getSku)
                    .toList();
            var productResponse = searchClient.getProductDetails(skus);
            if (productResponse == null || productResponse.getCode() != 200) {
                throw new RuntimeException("Cannot fetch product info");
            }

            Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductSkuVM::getSku,
                            Function.identity()
                    ));

            List<OrderItemResponse> itemResponses = orders.getItems().stream()
                    .map(item -> {
                        ProductSkuVM product = productMap.get(item.getSku());

                        return OrderItemResponse.builder()
                                .sku(item.getSku())
                                .productName(product != null ? product.getVariantName() : null)
                                .thumbnailUrl(product != null ? product.getThumbnailUrl() : null)
                                .quantity(item.getQuantity())
                                .sellPrice(item.getSellPrice())
                                .listPrice(item.getListPrice())
                                .addAt(item.getAddAt())
                                .build();
                    })
                    .toList();

            return OrderResponse.builder()
                    .orderId(orders.getOrderId())
                    .userId(orders.getUserId())
                    .orderStatus(orders.getOrderStatus())
                    .orderDate(orders.getOrderDate())
                    .totalPrice(orders.getTotalPrice())
                    .orderFee(orders.getOrderFee())
                    .orderDesc(orders.getOrderDesc())
                    .items(itemResponses)
                    .build();
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
            switch (status) {
                case FAILED -> {
                    rollbackVoucher(orderId);
                    sendKafkaEventToAnalysis(orders);
                }
                default -> decreaseInventory(orders);
            }
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
            List<Orders> orders = pageData.getContent();

            if(orders == null){
                return PageResponse.<OrderSummaryResponse>builder()
                        .currentPage(page)
                        .totalElements(0L)
                        .pageSize(size)
                        .data(List.of())
                        .build();
            }

            List<String> skus = orders.stream()
                    .flatMap(o -> o.getItems().stream())
                    .map(OrderItem::getSku)
                    .toList();

            var productResponse = searchClient.getProductDetails(skus);
            if (productResponse == null || productResponse.getCode() != 200) {
                throw new RuntimeException("Cannot fetch product info");
            }

            Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductSkuVM::getSku,
                            Function.identity()
                    ));

            List<OrderSummaryResponse> orderSummaries = orders.stream()
                    .map(order -> new OrderSummaryResponse(
                            order.getOrderId(),
                            order.getOrderDate(),
                            order.getTotalPrice(),
                            order.getItems().stream()
                                    .map(i -> {
                                        ProductSkuVM product = productMap.get(i.getSku());
                                        return new ItemSummaryResponse(
                                                product != null ? product.getVariantName() : null,
                                                product != null ? product.getThumbnailUrl() : null,
                                                i.getSellPrice(),
                                                i.getListPrice(),
                                                i.getQuantity()
                                        );
                                    })
                                    .toList()
                    ))
                    .toList();
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
            List<String> skus = orders.getItems()
                    .stream()
                    .map(OrderItem::getSku)
                    .toList();
            var productResponse = searchClient.getProductDetails(skus);
            if (productResponse == null || productResponse.getCode() != 200) {
                throw new RuntimeException("Cannot fetch product info");
            }

            Map<String, ProductSkuVM> productMap = productResponse.getResult().stream()
                    .collect(Collectors.toMap(
                            ProductSkuVM::getSku,
                            Function.identity()
                    ));

            List<OrderItemResponse> itemResponses = orders.getItems().stream()
                    .map(item -> {
                        ProductSkuVM product = productMap.get(item.getSku());

                        return OrderItemResponse.builder()
                                .sku(item.getSku())
                                .productName(product != null ? product.getVariantName() : null)
                                .thumbnailUrl(product != null ? product.getThumbnailUrl() : null)
                                .quantity(item.getQuantity())
                                .sellPrice(item.getSellPrice())
                                .listPrice(item.getListPrice())
                                .addAt(item.getAddAt())
                                .build();
                    })
                    .toList();

            return OrderResponse.builder()
                    .orderId(orders.getOrderId())
                    .userId(orders.getUserId())
                    .orderStatus(orders.getOrderStatus())
                    .orderDate(orders.getOrderDate())
                    .totalPrice(orders.getTotalPrice())
                    .orderFee(orders.getOrderFee())
                    .orderDesc(orders.getOrderDesc())
                    .items(itemResponses)
                    .build();
        }

        @Override
        public CheckVerifiedPurchase checkVerifiedPurchase(String userId, String productId) {
            List<Orders> orders = orderRepository.findAllByUserIdAndStatus(userId, Status.COMPLETED);
            if (orders.isEmpty()) {
                return CheckVerifiedPurchase.builder()
                        .isVerifiedPurchase(false)
                        .build();
            }
            var response = searchClient.getListSkuByProductById(productId);
            if (response == null || response.getCode() != 200) {
                throw new RuntimeException("Cannot fetch product info");
            }
            Set<String> purchasedSkus = orders.stream()
                    .flatMap(order -> order.getItems().stream())
                    .map(OrderItem::getSku)
                    .collect(Collectors.toSet());
            List<String> productSkus = response.getResult().getSkus();
            boolean verified = productSkus.stream()
                    .anyMatch(purchasedSkus::contains);
            return CheckVerifiedPurchase.builder()
                    .isVerifiedPurchase(verified)
                    .build();
        }

        @Override
        public GetListUserIdResponse getListUserId() {

            List<Status> validStatuses = List.of(
                    Status.COMPLETED
            );

            List<String> userIds =
                    orderRepository.findUserIdsOrderByOrderCountDesc(validStatuses);

            return GetListUserIdResponse.builder()
                    .userIds(userIds)
                    .build();
        }

        private String getUserId(){
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
            var apiResponse = userClient.getUserId(authHeader);
            return apiResponse.getResult().getUserId();
        }

        private List<OrderItemResponse>  buildOrderItemAndResponse(
                List<OrderItemRequest> requests,
                Orders orders,
                Map<String, ProductSkuVM> productMap
        ){
            if(requests == null || requests.isEmpty() || orders == null){
                orders.setItems(List.of());
                return List.of();
            }

            List<OrderItem> items = new ArrayList<>();
            List<OrderItemResponse> responses = new ArrayList<>();

            for(OrderItemRequest item : requests){
                ProductSkuVM product = productMap.get(item.getSku());
                if(product == null){
                    throw new RuntimeException("Product not exists: " + item.getSku());
                }
                if(item.getListPrice() == null || item.getSellPrice() == null
                        || product.getListPrice() == null || product.getSellPrice() == null
                        || item.getListPrice().compareTo(product.getListPrice()) != 0
                        || item.getSellPrice().compareTo(product.getSellPrice()) != 0){
                    throw new RuntimeException("Price for sku " + item.getSku() + " not valid");
                }

                items.add(OrderItem.builder()
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .sellPrice(product.getSellPrice())
                        .listPrice(product.getListPrice())
                        .addAt(LocalDateTime.now())
                        .orders(orders)
                        .build());

                responses.add(OrderItemResponse.builder()
                        .sku(item.getSku())
                        .productName(product.getVariantName())
                        .thumbnailUrl(product.getThumbnailUrl())
                        .quantity(item.getQuantity())
                        .sellPrice(product.getSellPrice())
                        .listPrice(product.getListPrice())
                        .addAt(LocalDateTime.now())
                        .build());
            }
            orders.setItems(items);
            return responses;
        }

        private void decreaseInventory(Orders orders) {
            for (OrderItem item : orders.getItems()) {
                ApiResponse<?> response = inventoryClient.decreaseStock(
                        InventoryAdjustmentRequest.builder()
                                .orderId(orders.getOrderId())
                                .sku(item.getSku())
                                .quantity(item.getQuantity())
                                .build()
                );

                if (response.getCode() != 200) {
                    throw new AppException(OrderErrorCode.INSUFFICIENT_STOCK);
                }
            }
        }

        private void increaseInventory(Orders orders) {
            for (OrderItem item : orders.getItems()) {
                inventoryClient.increaseStock(
                        InventoryAdjustmentRequest.builder()
                                .orderId(orders.getOrderId())
                                .sku(item.getSku())
                                .quantity(item.getQuantity())
                                .build()
                );
            }
        }

        private void sendKafkaEventToAnalysis(Orders orders){
            if(orders == null) return;
            OrderAnalysisEvent orderAnalysisEvent = OrderAnalysisEvent.builder()
                    .id(orders.getOrderId())
                    .userId(orders.getUserId())
                    .orderFee(orders.getOrderFee())
                    .totalPrice(orders.getTotalPrice())
                    .orderStatus(orders.getOrderStatus())
                    .orderDate(orders.getOrderDate())
                    .items(orders.getItems().stream().map(orderItem -> OrderItemEvent.builder()
                            .orderItemId(orderItem.getOrderItemId())
                            .orderId(orders.getOrderId())
                            .sku(orderItem.getSku())
                            .quantity(orderItem.getQuantity())
                            .sellPrice(orderItem.getSellPrice())
                            .listPrice(orderItem.getListPrice())
                            .addAt(orderItem.getAddAt())
                            .build()).toList())
                    .build();

            kafkaTemplate.send("order-analysis-event", orderAnalysisEvent).whenComplete(
                    (result, ex) -> {
                        if(ex != null)
                        {
                            System.err.println("Failed to send message" + ex.getMessage());
                        }else
                        {
                            System.err.println("send message successfully" + result.getProducerRecord());
                        }
                    });
        }

    }
