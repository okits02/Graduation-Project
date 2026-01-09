package com.okits02.analys_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "stock_in_analysis")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockInAnalysis {
    @Field(type = FieldType.Keyword)
    String id;
    @Field(type = FieldType.Keyword)
    String supplierName;
    @Field(type = FieldType.Keyword)
    String referenceCode;
    @Field(type = FieldType.Double)
    BigDecimal totalAmount;
    @Field(type = FieldType.Date)
    LocalDateTime createdAt = LocalDateTime.now();
    List<StockInItem> items;


    @PrePersist
    @PreUpdate
    public void calculateTotal() {
        this.totalAmount = items == null ? BigDecimal.ZERO : items.stream().map(
                item -> item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()))
        ).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
