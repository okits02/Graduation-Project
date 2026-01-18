package com.okits02.analys_service.viewmodel.dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductInfoResponse {
    String sku;
    String variantName;
    String thumbnail;
}
