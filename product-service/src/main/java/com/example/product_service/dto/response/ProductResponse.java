package com.example.product_service.dto.response;

import com.example.product_service.model.Specifications;
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
    String description;
    String videoUrl;
    String Brand;
    List<MediaResponse> mediaList;
    List<ProductVariantsResponse> variantsResponses;
    List<CategoryResponse> listCategory;
    List<Specifications> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
