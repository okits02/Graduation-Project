package com.example.product_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryAttribute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String value;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;
}
