package com.example.search_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Document(indexName = "product")
@Setting(settingPath = "static/es-settings.json")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Products {
    @Id
    String id;
    @Field(type = FieldType.Text)
    String name;
    @Field(type = FieldType.Text)
    String description;
    @Field(type = FieldType.Double)
    BigDecimal listPrice;
    @Field(type = FieldType.Double)
    BigDecimal sellPrice;
    @Field(type = FieldType.Nested)
    Set<Promotion> promotions;
    @Field(type = FieldType.Integer)
    Integer quantity;
    @Field(type = FieldType.Double)
    double avgRating;
    @Field(type = FieldType.Integer)
    Integer sold;
    @Field(type = FieldType.Keyword)
    List<String> imageList;
    @Field(type = FieldType.Nested)
    List<Category> categories;
    @Field(type = FieldType.Nested)
    List<Specification> specifications;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyy-MM-dd")
    LocalDate createAt;
    @Field(type = FieldType.Date,
            format = DateFormat.date,
            pattern = "yyy-MM-dd")
    LocalDate updateAt;

    public void calculatorSellPrice() {
        for(Promotion promotion : promotions) {
            if(promotion.getIsActive() == true) {
                if(promotion.getDiscountPercent() != null && promotion.getFixedAmount() == null) {
                    sellPrice = listPrice.multiply(promotion.discountPercent);
                }
                if(promotion.getFixedAmount() != null && promotion.getDiscountPercent() == null) {
                    sellPrice = listPrice.subtract(promotion.getFixedAmount());
                }
            }
        }
    }
}
