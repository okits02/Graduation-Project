package com.example.search_service.viewmodel.dto.request;


import jakarta.ws.rs.DefaultValue;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductGetByListIdRequest {
    List<String> productIds;
    @DefaultValue("1")
    int page;
    @DefaultValue("10")
    int size;
}
