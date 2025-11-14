package com.okits02.inventory_service.mapper;

import com.okits02.inventory_service.dto.response.InventoryTransactionResponse;
import com.okits02.inventory_service.model.InventoryTransaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryTransactionMapper {
    InventoryTransactionResponse toResponse(InventoryTransaction tran);
}
