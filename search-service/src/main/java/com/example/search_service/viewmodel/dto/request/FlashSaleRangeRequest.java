package com.example.search_service.viewmodel.dto.request;

import com.example.search_service.constant.SortType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlashSaleRangeRequest {
    Double minPrice;
    Double maxPrice;
    SortType sortType;
}
