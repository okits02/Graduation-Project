package com.example.profile_service.entity;

import com.example.profile_service.enums.WarrantyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "warranty")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWarranty {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String productId;
    private String sku;
    private String orderId;
    private String orderItemId;

    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private Integer warrantyMonths;

    private WarrantyStatus status;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id")
    UserProfile userProfile;
}