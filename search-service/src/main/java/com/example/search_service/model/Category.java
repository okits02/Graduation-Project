package com.example.search_service.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.List;

@Document(indexName = "category")
@Setting(settingPath = "static/es-settings.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Field(type = FieldType.Keyword)
    String id;
    @Field(type = FieldType.Keyword)
    String name;
    @Field(type = FieldType.Text)
    String description;
    @Field(type = FieldType.Text)
    String imageUrl;
    @Field(type = FieldType.Boolean)
    Boolean special;
    @Field(type = FieldType.Keyword)
    String parentId;
    @Field(type = FieldType.Keyword)
    List<String> childrenId;
}
