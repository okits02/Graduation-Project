package com.example.order_service.mapper;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.model.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(Orders orders);
}
