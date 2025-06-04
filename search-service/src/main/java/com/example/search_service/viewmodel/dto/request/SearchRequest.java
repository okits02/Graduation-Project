package com.example.search_service.viewmodel.dto.request;

import com.example.search_service.constant.SortType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchRequest {
    String keyword;
    String category;
    String attributes;
    String minPrice;
    String maxPrice;
    SortType sortType;
}
