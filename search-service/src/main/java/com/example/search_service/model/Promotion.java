package com.example.search_service.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    @Field(type = FieldType.Keyword)
    String id;
    @Field(type = FieldType.Text)
    String name;
    @Field(type = FieldType.Text)
    String descriptions;
    @Field(type = FieldType.Double)
    BigDecimal discountPercent;
    @Field(type = FieldType.Keyword)
    String applyTo;
    @Field(type = FieldType.Double)
    BigDecimal fixedAmount;
    @Field(type = FieldType.Boolean)
    Boolean active;
}
