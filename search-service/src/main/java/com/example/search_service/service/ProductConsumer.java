package com.example.search_service.service;

import com.example.search_service.Repository.ProductsRepository;
import com.example.search_service.model.Products;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductConsumer {
    private final ProductsRepository productsRepository;

    @KafkaListener(topics = "create-product")
    public void consumerCreateProduct(String productEvent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Products products = null;
        try {
            products = objectMapper.readValue(productEvent, Products.class);
        } catch (JsonMappingException e)
        {
            throw new RuntimeException(e);
        }
        productsRepository.save(products);
    }
}
