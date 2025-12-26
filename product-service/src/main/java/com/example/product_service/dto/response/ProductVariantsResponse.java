package com.example.product_service.dto.response;

import com.example.product_service.model.Specifications;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantsResponse {
    String variantName;
    String sku;
    BigDecimal price;
    Integer sold;
    String thumbnail;
    List<Specifications> bestSpecifications;
    Boolean inStock;
    LocalDate createAt;
    LocalDate updateAt;
}
