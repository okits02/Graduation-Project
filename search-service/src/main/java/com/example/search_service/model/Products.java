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
        if (listPrice == null || promotions == null) return;
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal fixedAmount = BigDecimal.ZERO;

        for (Promotion promotion : promotions) {
            if (Boolean.TRUE.equals(promotion.getActive())) {
                if (promotion.getDiscountPercent() != null && promotion.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
                    discount = discount.add(promotion.getDiscountPercent().divide(BigDecimal.valueOf(100)));
                }
                if (promotion.getFixedAmount() != null && promotion.getFixedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    fixedAmount = fixedAmount.add(promotion.getFixedAmount());
                }
            }
        }
        if (discount.compareTo(BigDecimal.ZERO) > 0) {
            sellPrice = listPrice.multiply(BigDecimal.ONE.subtract(discount));
        }
        if (fixedAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (sellPrice == null) sellPrice = listPrice;
            sellPrice = sellPrice.subtract(fixedAmount);
        }
    }

}
