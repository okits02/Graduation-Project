package com.example.product_service.kafka;

import com.example.product_service.dto.response.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductEvent {
    String id;
    String name;
    String color;
    String description;
    BigDecimal listPrice;
    Integer quantity;
    double avgRating;
    Integer sold;
    List<String> imageList;
    List<CategoryResponse> categories;
    Map<String, String> specifications;
    LocalDate createAt;
    LocalDate updateAt;
}
