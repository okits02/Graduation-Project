package com.example.promotion_service.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryLevelValidateRequest {
    private List<String> categoryIds;
}
