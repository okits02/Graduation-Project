package com.example.product_service.model;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    @Field("description")
    String description;
    @Field("special")
    boolean special;
    @Field("image_url")
    String imageUrl;
    @Field("parent_category_id")
    String parentId;
    @Field("children_category_id")
    Set<String> childrenId;
}
