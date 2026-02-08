package com.example.search_service.viewmodel.dto.request;

import com.example.search_service.constant.SortType;
import com.example.search_service.viewmodel.dto.SpecificationFilterDTO;
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
    List<String> brandName;
    String category;
    List<SpecificationFilterDTO> attributes;
    String ownerId;
    String ownerType;
    Boolean flashSale;
    Double minPrice;
    Double maxPrice;
    SortType sortType;
}
