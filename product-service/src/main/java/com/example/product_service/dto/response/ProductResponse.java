package com.example.product_service.dto.response;

import com.example.product_service.model.Category;
import com.example.product_service.model.Image;
import jakarta.persistence.*;
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
public class ProductResponse {
    String id;
    String name;
    String description;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    Float discount;
    List<String> imageList;
    List<String> listCategory;
    Map<String, String> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
