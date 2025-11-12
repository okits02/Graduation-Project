package com.okits02.inventory_service.mapper;

import com.okits02.inventory_service.dto.response.StockInItemResponse;
import com.okits02.inventory_service.model.StockInItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockInItemMapper {
    StockInItemResponse toResponse(StockInItem item);
}
