package com.okits02.analys_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

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
    @Field(type = FieldType.Date, format = DateFormat.date_time,pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt;
}
