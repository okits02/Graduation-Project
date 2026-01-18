package com.okits02.analys_service.viewmodel.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableAnalysisItem {
    String sku;
    String variantName;
    String thumbnail;
    Integer totalQuantity;

}
