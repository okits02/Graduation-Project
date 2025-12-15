package com.example.search_service.viewmodel;

import com.example.search_service.model.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryGetVM {
    String id;
    String name;
    String description;
    String imageUrl;
    String parentId;
    List<String> childrenId;
    public static CategoryGetVM fromEntity(Category category){
        return CategoryGetVM.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParentId())
                .childrenId(category.getChildrenId())
                .build();
    }
}
