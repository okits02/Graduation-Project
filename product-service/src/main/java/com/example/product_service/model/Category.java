package com.example.product_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JoinColumn(name = "id_category")
    String id;
    @JoinColumn(name = "name_category")
    String name;
    @JoinColumn(name = "description")
    String description;
    @JoinColumn(name = "image_url")
    String imageUrl;
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    Set<Category> subCategories;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_category_id")
    Category parentCategory;

    @ToString.Exclude
    @OneToMany(mappedBy = "category")
    Set<Products> products;
}
