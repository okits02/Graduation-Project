package com.example.search_service.viewmodel.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchCateRequest {
    String name;
    int size;
    int page;
}
