package com.example.rating_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(nullable = false, length = 2000)
    String content;

    @Column(nullable = false)
    String productId;

    @Column(nullable = false)
    String userId;

    // null = comment gốc
    String parentId;
    @CreationTimestamp
    LocalDateTime createdAt;

    Boolean isDeleted = false;
}
