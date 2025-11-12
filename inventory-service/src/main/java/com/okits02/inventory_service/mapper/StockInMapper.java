package com.okits02.inventory_service.mapper;

import com.okits02.inventory_service.dto.request.StockInCreationRequest;
import com.okits02.inventory_service.model.StockIn;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockInMapper {
    StockIn toStockIn(StockInCreationRequest request);
}
