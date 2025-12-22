package com.example.product_service.model;

import com.example.product_service.enums.SpecGroup;
import com.example.product_service.enums.SpecType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products_details")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Specifications {
    String key;
    String value;
    SpecType type;
    SpecGroup group;
}
