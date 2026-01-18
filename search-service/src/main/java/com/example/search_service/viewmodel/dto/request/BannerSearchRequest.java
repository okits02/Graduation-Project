package com.example.search_service.viewmodel.dto.request;

import com.example.search_service.constant.SortType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BannerSearchRequest {
    Double minPrice;
    Double maxPrice;
    SortType sortType;
    String ownerId;
    String ownerType;
}
