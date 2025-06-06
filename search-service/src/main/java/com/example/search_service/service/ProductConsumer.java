package com.example.search_service.service;

import com.example.search_service.Repository.ProductsRepository;
import com.example.search_service.mapper.ProductsMapper;
import com.example.search_service.model.Products;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductConsumer {
    private final ProductsRepository productsRepository;
    private final ProductsMapper productsMapper;

    @KafkaListener(topics = "create-product")
    public void consumerCreateProduct(String productEvent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ProductEventDTO productEventDTO;
        try {
            productEventDTO = objectMapper.readValue(productEvent, ProductEventDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot deserialize ProductEvent: " + e.getMessage(), e);
        }
        Products products = productsMapper.toProducts(productEventDTO);
        productsRepository.save(products);
    }
}
