package com.example.product_service.utils;

import com.example.product_service.model.Products;

import java.util.Map;
import java.util.UUID;

public class SkuGenerator {
    public static String generateSku() {
        return "SKU-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
    }
}
