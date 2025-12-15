package com.example.search_service.viewmodel.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryGetByListIdRequest {
    List<String> categoryIds;
}
