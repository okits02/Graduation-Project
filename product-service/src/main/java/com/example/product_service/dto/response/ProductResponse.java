package com.example.product_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  ProductResponse {
    String id;
    String name;
    String color;
    String description;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    String videoUrl;
    Float discount;
    String thumbNail;
    Boolean inStock;
    List<MediaResponse> mediaList;
    List<CategoryResponse> listCategory;
    Map<String, String> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
