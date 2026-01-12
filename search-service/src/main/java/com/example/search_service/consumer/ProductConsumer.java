package com.example.search_service.consumer;


import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.DeleteProductEvent;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductConsumer {
    private final ProductService productService;

    @KafkaListener(topics = "product-event",
            containerFactory = "productKafkaListenerContainerFactory")
    public void consumerCreateProduct(String productEvent) throws IOException {
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
        log.info("Consumer received message: {}", productEvent);
        switch (productEventDTO.getEventType()){
            case "CREATED" -> {
                try {
                    productService.createProduct(productEventDTO);
                } catch (Exception e) {
                    log.error("Error processing CREATED event: {}", e.getMessage());
                }
            }
            case "UPDATED" -> {
                try {
                    productService.updateProduct(productEventDTO);
                } catch (Exception e) {
                    log.error("Error processing UPDATED event: {}", e.getMessage());
                }
            }
            case "DELETED" -> {
                try {
                    productService.deleteProduct(productEventDTO.getId());
                } catch (Exception e) {
                    log.error("Error processing DELETED event: {}", e.getMessage());
                }
            }
        }
    }

    @KafkaListener(topics = "product-delete-topics",
            containerFactory = "deleteProductKafkaListenerContainerFactory")
    public void consumerDeleteProduct(String deleteEvent){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        DeleteProductEvent deleteProductEvent;
        try {
            deleteProductEvent = objectMapper.readValue(deleteEvent, DeleteProductEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot deserialize ProductEvent: " + e.getMessage(), e);
        }
        switch (deleteProductEvent.getDeleteEventType()){
            case "DELETE_LIST" -> {
                productService.deleteProductByList(deleteProductEvent.getProductId());
            }
            case "DELETE_ALL" -> {
                productService.deleteAllProduct();
            }
        }
    }

}
