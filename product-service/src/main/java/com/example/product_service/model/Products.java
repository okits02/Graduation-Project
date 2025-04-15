package com.example.product_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String brand;
    String model;
    String description;
    BigDecimal price;
    Integer quantity;
    Integer sold;
    String thumbnailUrl;
    String warrantyPeriod;
    Float discount;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    List<ProductAttribute> attributeList;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    LocalDate createAt;
    LocalDate updateAt;
}
