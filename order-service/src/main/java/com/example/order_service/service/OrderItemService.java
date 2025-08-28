package com.example.order_service.service;

import com.example.order_service.dto.ProductGetVM;
import com.example.order_service.dto.response.ApiResponse;
import com.example.order_service.model.OrderItem;
import com.example.order_service.model.Orders;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.repository.httpClient.ProductClient;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final ProductClient productClient;


    public OrderItem save(String productId, Integer quantity, Orders orders){
        OrderItem orderItem = orderItemRepository.findByProductId(productId);
        OrderItem newOrderItem = new OrderItem();
        if(orderItem != null){
            Integer quantityItem = orderItem.getQuantity();
            quantityItem = quantity + quantityItem;
            orderItem.setQuantity(quantityItem);
            orderItem.calculatorSellPrice();
            return orderItemRepository.save(orderItem);
        }else{
            ServletRequestAttributes servletRequestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
            var productResponse = productClient.getProductDetails(authHeader, productId).getBody();
            if(productResponse == null || productResponse.getCode() != 200){
                throw new RuntimeException("Product is not exists!");
            }
            ProductGetVM productGetVM = productResponse.getResult();
            newOrderItem.setProductId(productId);
            newOrderItem.setProductName(productGetVM.getName());
            newOrderItem.setListPrice(productGetVM.getListPrice());
            newOrderItem.setSellPrice(productGetVM.getSellPrice());
            newOrderItem.setQuantity(quantity);
            newOrderItem.setOrders(orders);
            newOrderItem.calculatorSellPrice();
        }
        return orderItemRepository.save(newOrderItem);
    }


}
