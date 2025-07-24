package com.example.search_service.viewmodel.dto.request;

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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String id;
    String name;
    String description;
    BigDecimal listPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    List<String> imageList;
    List<String> categories;
    Map<String, String> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
