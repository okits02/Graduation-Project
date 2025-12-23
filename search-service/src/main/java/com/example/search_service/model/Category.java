package com.example.search_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
