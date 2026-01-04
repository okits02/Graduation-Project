package com.okits02.inventory_service.mapper;

import com.okits02.inventory_service.dto.request.InventoryRequest;
import com.okits02.inventory_service.dto.response.InventoryResponse;
import com.okits02.inventory_service.model.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    InventoryResponse toInventoryResponse(Inventory inventory);
}
