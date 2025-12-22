package com.example.product_service.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "products_variants")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product_variants {
    @Id
    String id;
    @Field("product_id")
    String productId;
    @Field("variant_name")
    String variant_name;
    @Field("sku")
    String sku;
    @Field("price")
    BigDecimal price;
    @Field("sold_quantity")
    Integer sold;
    @Field("thumbnail")
    String thumbnail;
    @Field("best_specifications")
    List<Specifications> bestSpecifications;
    @Field("in_stock")
    Boolean inStock;
    @CreatedDate
    @Field("create_at")
    LocalDate createAt;
    @LastModifiedDate
    @Field("update_at")
    LocalDate updateAt;
}
