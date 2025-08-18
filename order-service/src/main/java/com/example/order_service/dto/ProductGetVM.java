package com.example.order_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductGetVM {
    String id;
    String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal listPrice;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal sellPrice;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate createAt;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate updateAt;
}
