package com.okits02.inventory_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    String id;
    String sku;
    String variantName;
    String thumbnail;
    Integer quantity;
}
