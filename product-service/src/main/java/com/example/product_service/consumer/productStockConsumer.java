package com.example.product_service.consumer;

import com.example.product_service.dto.ChangeInStockEvent;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class productStockConsumer {
    private final ProductService productService;

    @KafkaListener(topics = "change-status-event",
        containerFactory = "changeIsStockKafkaListenerContainerFactory")
    public void consumerChangeInStock(String productEvent){
        ObjectMapper objectMapper = new ObjectMapper();
        ChangeInStockEvent changeInStockEvent = null;
        try {
            changeInStockEvent = objectMapper.readValue(productEvent, ChangeInStockEvent.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}
