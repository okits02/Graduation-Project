package com.example.search_service.viewmodel;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryDetailsVM {
    String id;
    String name;
    String description;
    String imageUrl;
    String parentId;
    List<CategoryGetVM> childrenId;
}
