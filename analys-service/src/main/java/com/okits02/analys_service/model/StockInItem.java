package com.okits02.analys_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;

@Document(indexName = "stock_in_item_analysis")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockInItem {
    @Field(type = FieldType.Keyword)
    String id;
    @Field(type = FieldType.Keyword)
    String sku;
    @Field(type = FieldType.Integer)
    Integer quantity;
    @Field(type = FieldType.Double)
    BigDecimal unitCost;
    @Field(type = FieldType.Double)
    BigDecimal totalCost;
    @PrePersist
    @PreUpdate
    public void calculate() {
        totalCost = unitCost.multiply(BigDecimal.valueOf(quantity));
    }
}
