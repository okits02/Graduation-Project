package com.example.search_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    @Field(type = FieldType.Double)
    BigDecimal listPrice;
    @Field(type = FieldType.Double)
    BigDecimal sellPrice;
    @Field(type = FieldType.Integer)
    Integer quantity;
    @Field(type = FieldType.Double)
    double avgRating;
    @Field(type = FieldType.Integer)
    Integer sold;
    @Field(type = FieldType.Double)
    Float discount;
    @Field(type = FieldType.Keyword)
    List<String> imageList;
    @Field(type = FieldType.Keyword)
    List<String> categories;
    @Field(type = FieldType.Object)
    Map<String, String> specifications;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyy-MM-dd")
    LocalDate createAt;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyy-MM-dd")
    LocalDate updateAt;
}
