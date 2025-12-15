package com.example.product_service.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CategoryLevelValidateRequest {
    private List<String> categoryIds;
}
