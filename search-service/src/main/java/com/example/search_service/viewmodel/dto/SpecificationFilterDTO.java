package com.example.search_service.viewmodel.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecificationFilterDTO {
    String key;
    String value;
    String group;
    String type;
}
