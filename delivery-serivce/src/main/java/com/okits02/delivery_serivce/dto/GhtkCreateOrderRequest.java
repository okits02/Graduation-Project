package com.okits02.delivery_serivce.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhtkCreateOrderRequest {
    List<ProductDTO> products;
    OrderDTO order;
}
