package com.example.search_service.viewmodel;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductGetListVM<T> {
    List<T> productGetVMList;
    int currentPages;
    int totalPage;
    int pageSize;
    Long totalElements;
    Map<String, Map<String, Long>> specificationAggregations;
    Map<String, Long> categoriesAggregations;
}
