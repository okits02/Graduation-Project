package com.example.product_service.model;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "categories")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    String id;
    @Field("name_category")
    String name;
    @Field("thumbnail_url")
    String thumbnail;
    @Field("description")
    String description;
    @Field("special")
    Boolean special;
    @Field("parent_category_id")
    String parentId;
    @Field("children_category_id")
    Set<String> childrenId = new HashSet<>();
}
