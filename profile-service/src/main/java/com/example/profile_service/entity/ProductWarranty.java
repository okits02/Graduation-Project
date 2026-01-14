package com.example.profile_service.entity;

import com.example.profile_service.enums.WarrantyStatus;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Node("product_warranty")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductWarranty {

    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    private String productId;
    private String sku;
    private String orderId;
    private String orderItemId;

    private LocalDate warrantyStartDate;
    private LocalDate warrantyEndDate;
    private Integer warrantyMonths;

    @Property("status")
    private WarrantyStatus status;

    private LocalDateTime createdAt;
}