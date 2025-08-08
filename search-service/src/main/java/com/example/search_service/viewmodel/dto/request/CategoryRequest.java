package com.example.search_service.viewmodel.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {
    String id;
    String name;
    String description;
    String imageUrl;
    String parentId;
    List<String> childrenId;
}
