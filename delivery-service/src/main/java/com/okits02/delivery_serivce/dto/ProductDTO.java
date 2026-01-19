package com.okits02.delivery_serivce.dto;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO {
    String name;
    Double weight;
    Integer quantity;
}
