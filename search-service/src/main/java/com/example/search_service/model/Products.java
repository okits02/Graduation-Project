package com.example.search_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
    @Field(type = FieldType.Text)
    String description;
    @Field(type = FieldType.Nested)
    Set<Promotion> promotions;
    @Field(type = FieldType.Double)
    double avgRating;
    @Field(type = FieldType.Nested)
    List<String> categoriesId;
    @Field(type = FieldType.Nested)
    List<Specification> specifications;
    @Field(type = FieldType.Nested)
    List<Product_variants> productVariants;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyy-MM-dd")
    LocalDate createAt;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyy-MM-dd")
    LocalDate updateAt;


}
