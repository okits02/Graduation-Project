package com.example.search_service.viewmodel.dto;

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
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductEventDTO {
    String id;
    String name;
    String color;
    String description;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    List<String> categoriesId;
    Map<String, String> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
