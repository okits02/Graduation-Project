package com.example.product_service.dto.request;

import com.example.product_service.enums.SpecGroup;
import com.example.product_service.enums.SpecType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecificationRequest {
    String key;
    String value;
    SpecType type;
    SpecGroup group;
}
