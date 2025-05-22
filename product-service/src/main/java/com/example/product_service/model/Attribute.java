package com.example.product_service.model;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column
    String name;
    String categoryId;
    String unit;
    @ManyToOne
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    Category category;
}
