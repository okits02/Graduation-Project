package com.example.search_service.viewmodel.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantResponse {
    String id;
    String variantName;
    String sku;
    String color;
    BigDecimal price;
    BigDecimal sellPrice;
    Integer sold;
    String thumbnail;
    Boolean inStock;
    Integer quantity;
    LocalDate createAt;
    LocalDate updateAt;
}
