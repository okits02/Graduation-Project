package com.example.search_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Document(indexName = "product")
@.
Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class products {
    @Id
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
