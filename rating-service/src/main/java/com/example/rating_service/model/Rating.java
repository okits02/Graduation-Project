package com.example.rating_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "rating")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(length = 2000)
    String content;
    Double ratingScore;
    String productId;
    String userId;
    boolean isVerifiedPurchase;
    List<String> imageUrl;
    @CreationTimestamp
    LocalDate createdAt;
}
