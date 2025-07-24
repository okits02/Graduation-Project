package com.example.search_service.viewmodel.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    String description;
    BigDecimal listPrice;
    BigDecimal sellPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    Float discount;
    List<String> imageList;
    List<String> categories;
    Map<String, String> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
