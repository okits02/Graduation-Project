package com.example.search_service.consumer;


import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.dto.ApplyThumbnailEventDTO;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
import com.example.search_service.viewmodel.dto.request.ApplyThumbnailRequest;
import com.example.search_service.viewmodel.dto.request.DeleteProductEventDTO;
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

    @KafkaListener(topics = "product-create-event",
            containerFactory = "createKafkaListenerContainerFactory")
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
        productService.createProduct(productEventDTO);
    }

    @KafkaListener( topics = "product-update-event",
            containerFactory = "updateKafkaListenerContainerFactory")
    public void consumerUpdateProduct(String productEvent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ProductEventDTO productEventDTO;
        productEventDTO = objectMapper.readValue(productEvent, ProductEventDTO.class);
        productService.updateProduct(productEventDTO);
    }

    @KafkaListener( topics = "product-delete-event",
            containerFactory = "deleteKafkaListenerContainerFactory")
    public void consumerDeleteProduct(String productEvent) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        DeleteProductEventDTO deleteProductEventDTO = null;
        deleteProductEventDTO = objectMapper.readValue(productEvent, DeleteProductEventDTO.class);
        productService.deleteProduct(deleteProductEventDTO.getProductId());
    }

}
