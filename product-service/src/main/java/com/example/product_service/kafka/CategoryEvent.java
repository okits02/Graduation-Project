package com.example.product_service.kafka;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryEvent {
    String eventType;
    String id;
    String name;
    String descriptions;
    String imageUrl;
    String parentId;
    List<String> childrentId;
}
