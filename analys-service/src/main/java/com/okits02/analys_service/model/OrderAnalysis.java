package com.okits02.analys_service.model;

import com.okits02.analys_service.enums.Status;
import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "order_analysis")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderAnalysis {
    @Field(type = FieldType.Keyword)
    private String id;
    @Field(type = FieldType.Keyword)
    private String orderId;
    @Field(type = FieldType.Keyword)
    private String userId;
    @Field(type = FieldType.Keyword)
    private Status orderStatus;
    @Field(type = FieldType.Double)
    private BigDecimal orderFee;
    @Field(type = FieldType.Double)
    private BigDecimal totalPrice;
    @Field(type = FieldType.Date, format = DateFormat.date_time,pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime orderDate;
}
