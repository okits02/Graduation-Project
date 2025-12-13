package com.example.search_service.consumer;

import com.example.search_service.service.CategoryService;
import com.example.search_service.viewmodel.dto.CategoryEventDTO;
import com.example.search_service.viewmodel.dto.ProductEventDTO;
import com.example.search_service.viewmodel.dto.request.ProductRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CategoryConsumer {
    private final CategoryService categoryService;

    @KafkaListener(topics = "category-event",
            containerFactory = "categoryKafkaListenerContainerFactory")
    public void categoryListener(String categoryEvent) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CategoryEventDTO categoryEventDTO;
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            categoryEventDTO = objectMapper.readValue(categoryEvent, CategoryEventDTO.class);
        }catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot deserialize ProductEvent: " + e.getMessage(), e);
        }
        switch (categoryEventDTO.getEventType()) {
            case "CATEGORY_CREATED" -> categoryService.indexCategory(categoryEventDTO);
            case "CATEGORY_UPDATE" -> categoryService.updateCategory(categoryEventDTO);
            case "CATEGORY_DELETE" -> categoryService.deleteCategory(categoryEventDTO.getId());
        }
    }

}
