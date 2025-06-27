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
import java.util.Map;

@Document(collection = "products")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Products {
    @Id
    String id;
    @Field("name_product")
    String name;
    @Field("description")
    String description;
    @Field("list_price")
    BigDecimal listPrice;
    Integer quantity;
    @Field("avg_rating")
    double avgRating;
    @Field("sold_quantity")
    Integer sold;
    @Field("thumb_nail")
    String thumbNail;
    @Field("images")
    List<String> imageList;
    String categoryId;
    Map<String, String> specifications;
    @CreatedDate
    @Field("create_at")
    LocalDate createAt;
    @LastModifiedDate
    @Field("update_at")
    LocalDate updateAt;
}
