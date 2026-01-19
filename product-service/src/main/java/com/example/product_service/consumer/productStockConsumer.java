package com.example.product_service.consumer;

import com.example.product_service.dto.ChangeStatusStockEvent;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class productStockConsumer {
    private final ProductService productService;

    @KafkaListener(topics = "change-status-event",
        containerFactory = "changeIsStockKafkaListenerContainerFactory")
    public void consumerChangeInStock(String productEvent){
        log.info("Received message from Kafka topic change-status-event: {}", productEvent);
        ObjectMapper objectMapper = new ObjectMapper();
        ChangeStatusStockEvent changeInStockEvent = null;
        try {
            changeInStockEvent = objectMapper.readValue(productEvent, ChangeStatusStockEvent.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        productService.changeStatusInStock(changeInStockEvent.getSku(), changeInStockEvent.getInStock());
    }
}
