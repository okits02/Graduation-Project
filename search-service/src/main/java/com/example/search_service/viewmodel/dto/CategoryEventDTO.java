package com.example.search_service.viewmodel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryEventDTO {
    String eventType;
    String id;
    String name;
    String descriptions;
    String imageUrl;
    String parentId;
    List<String> childrentId;
}
