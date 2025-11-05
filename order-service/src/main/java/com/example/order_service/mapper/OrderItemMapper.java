package com.example.order_service.mapper;

import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.model.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItem toOrderItem(OrderItemRequest request);
}
