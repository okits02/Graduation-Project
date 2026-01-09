package com.okits02.analys_service.model;

import com.okits02.analys_service.enums.TransactionType;
import com.okits02.analys_service.enums.ReferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.time.LocalDateTime;

@Document(indexName = "inventory_transaction_analysis")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryTransactionAnalysis {
    @Field(type = FieldType.Keyword)
    String id;
    @Field(type = FieldType.Keyword)
    String sku;
    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    TransactionType transactionType;

    @Field(type = FieldType.Integer)
    @Column(nullable = false)
    Integer quantity;

    @Field(type = FieldType.Integer)
    String referenceId;

    @Field(type = FieldType.Integer)
    @Enumerated(EnumType.STRING)
    ReferenceType referenceType;

    @Field(type = FieldType.Date)
    LocalDateTime createdAt = LocalDateTime.now();
}
