package com.example.search_service.viewmodel.dto;

import com.example.search_service.model.Product_variants;
import com.example.search_service.model.Specification;
import com.example.search_service.viewmodel.dto.request.CategoryRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductEventDTO {
    String id;
    String name;
    String description;
    Integer quantity;
    double avgRating;
    List<String> categoriesId;
    List<Specification> specifications;
    List<Product_variants> productVariants;
    LocalDate createAt;
    LocalDate updateAt;
}
