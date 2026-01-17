package com.okits02.analys_service.model;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

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
    @Field(type = FieldType.Date)
    private LocalDateTime addAt;
}
