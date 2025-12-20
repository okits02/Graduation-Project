package com.example.product_service.utils;

import com.example.product_service.model.Products;

import java.util.Map;

public class SkuGenerator {
    public static String generateSku(Products product) {

        String categoryPart = extractCategory(product);
        String modelPart = normalize(product.getName());
        String colorPart = normalize(product.getColor());
        String specPart = extractSpecs(product.getSpecifications());

        return String.join("-",
                categoryPart,
                modelPart,
                colorPart,
                specPart
        ).replaceAll("-+", "-");
    }

    private static String extractCategory(Products product) {
        if (product.getCategoryId() == null || product.getCategoryId().isEmpty()) {
            return "GEN";
        }
        return product.getCategoryId().iterator().next().toUpperCase();
    }

    private static String extractSpecs(Map<String, String> specs) {
        if (specs == null || specs.isEmpty()) {
            return "";
        }

        return specs.values().stream()
                .map(SkuGenerator::normalize)
                .sorted()
                .reduce((a, b) -> a + "-" + b)
                .orElse("");
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.toUpperCase()
                .replaceAll("\\s+", "")
                .replaceAll("GB", "")
                .replaceAll("[^A-Z0-9]", "");
    }
}
