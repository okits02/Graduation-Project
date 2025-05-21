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
    @Column(name = "id_product")
    String id;
    @Column(name = "name_product")
    String name;
    @Column(name = "description")
    String description;
    @Column(name = "list_price")
    BigDecimal listPrice;
    @Column(name = "sell_price")
    BigDecimal sellPrice;
    Integer quantity;
    @Column(name = "avg_rating")
    double avgRating;
    @Column(name = "sold_quantity")
    Integer sold;
    Float discount;
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<Image> imageList;
    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
    @Column(name = "create_at")
    LocalDate createAt;
    @Column(name = "update_at")
    LocalDate updateAt;
}
