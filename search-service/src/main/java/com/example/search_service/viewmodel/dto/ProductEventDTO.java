package com.example.search_service.viewmodel.dto;

import com.example.search_service.model.ProductVariants;
import com.example.search_service.model.Specification;
import com.example.search_service.viewmodel.CateItemDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductEventDTO {
    String eventType;
    String id;
    String name;
    String brand;
    String description;
    Integer quantity;
    Double avgRating;
    String videoUrl;
    Integer warranty;
    List<CateItemDTO> categories;
    List<Specification> specifications;
    List<ProductVariants> productVariants;
    LocalDate createAt;
    LocalDate updateAt;
}
