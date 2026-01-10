package com.example.search_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Document(indexName = "product")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Products {
    @Id
    String id;
    @Field(type = FieldType.Text)
    String name;
    @Field(type = FieldType.Keyword)
    private String brand;
    @Field(type = FieldType.Text)
    String description;
    @Field(type = FieldType.Object)
    Set<Promotion> promotions;
    @Field(type = FieldType.Double)
    Double avgRating;
    @Field(type = FieldType.Keyword)
    List<String> categoriesId;
    @Field(type = FieldType.Nested)
    List<Specification> specifications;
    @Field(type = FieldType.Nested)
    List<ProductVariants> productVariants;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyyy-MM-dd")
    LocalDate createAt;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyyy-MM-dd")
    LocalDate updateAt;


}
