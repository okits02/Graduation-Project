package com.example.product_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductUpdateRequest {
    String id;
    String name;
    String description;
    String brandName;
    String videoUrl;
    double avgRating;
    LocalDate warrantyStartDate;
    LocalDate warrantyEndDate;
    Set<String> categoryId;
    List<SpecificationRequest> specifications;
    List<ProductVariantsRequest> productVariants;
}
