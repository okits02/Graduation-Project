package com.example.search_service.viewmodel.dto.request;

import com.example.search_service.constant.SortType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchRequest {
    String keyword;
    String color;

    String category;
    List<Map<String, String>> attributes;
    Double minPrice;
    Double maxPrice;
    SortType sortType;
}
