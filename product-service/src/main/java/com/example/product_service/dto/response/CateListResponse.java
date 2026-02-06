package com.example.product_service.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CateListResponse {
    String id;
    String name;
    String description;
    String thumbnail;
    String parentId;
    Boolean special;
    List<CateListResponse> children = new ArrayList<>();
}
