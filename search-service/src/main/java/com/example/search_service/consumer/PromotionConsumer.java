package com.example.search_service.consumer;

import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.dto.ApplyPromotionEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PromotionConsumer {
    private final ProductService productService;

    @KafkaListener(topics = "promotion-create-event",
            containerFactory = "applyPromotionKafkaListenerContainerFactory")
    public void applyPromotionConsumer(String promotionEvent) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApplyPromotionEventDTO applyPromotionEventDTO = null;
        try {
            applyPromotionEventDTO = objectMapper.readValue(promotionEvent, ApplyPromotionEventDTO.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        productService.createPromotion(applyPromotionEventDTO);
    }
}
