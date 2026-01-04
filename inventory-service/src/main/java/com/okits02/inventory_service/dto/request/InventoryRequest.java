package com.okits02.inventory_service.dto.request;

import com.okits02.inventory_service.validator.QuantityConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRequest {
    String sku;
    @QuantityConstraint(message = "Quantity must not null and greater than 0")
    Integer quantity;
}
