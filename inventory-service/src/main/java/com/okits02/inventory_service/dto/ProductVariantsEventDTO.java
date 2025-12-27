package com.okits02.inventory_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantsEventDTO {
    String sku;
    String variantName;
}
