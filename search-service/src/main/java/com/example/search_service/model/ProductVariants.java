package com.example.search_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariants {
    @Id
    String id;
    @Field(type = FieldType.Text)
    String variantName;
    @Field(type = FieldType.Keyword)
    String sku;
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    BigDecimal price;
    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    BigDecimal sellPrice;
    @Field(type = FieldType.Integer)
    Integer sold;
    @Field(type = FieldType.Text)
    String thumbnail;
    @Field(type = FieldType.Nested)
    List<Specification> bestSpecifications;
    @Field(type = FieldType.Boolean)
    Boolean inStock;
    @CreatedDate
    @Field("create_at")
    LocalDate createAt;
    @LastModifiedDate
    @Field("update_at")
    LocalDate updateAt;

}
