package com.example.order_service.mapper;

import com.example.order_service.dto.request.OrderCreationRequest;
import com.example.order_service.dto.request.RePaymentForOrder;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.model.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderResponse toOrderResponse(Orders orders);
    void update(@MappingTarget Orders orders, RePaymentForOrder rePaymentForOrder);
}
