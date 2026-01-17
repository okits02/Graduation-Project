package com.okits02.analys_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "order_item_analysis")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @Field(type = FieldType.Keyword)
    private String orderItemId;
    @Field(type = FieldType.Keyword)
    private String orderId;
    @Field(type = FieldType.Keyword)
    private String sku;
    @Field(type = FieldType.Keyword)
    private String variantName;
    @Field(type = FieldType.Text)
    private String thumbnail;
    @Field(type = FieldType.Integer)
    private Integer quantity;
    @Field(type = FieldType.Double)
    private BigDecimal listPrice;
    @Field(type = FieldType.Double)
    private BigDecimal sellPrice;
    @Field(type = FieldType.Date, format = DateFormat.date_time,pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime addAt;
}
