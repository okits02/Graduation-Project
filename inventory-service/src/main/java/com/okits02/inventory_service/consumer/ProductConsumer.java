package com.okits02.inventory_service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okits02.inventory_service.dto.ProductDeleteEventDTO;
import com.okits02.inventory_service.dto.ProductEventDTO;
import com.okits02.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductConsumer {
    private final InventoryService inventoryService;

    @KafkaListener(topics = "product-event",
            containerFactory = "InventoryKafkaListenerContainerFactory")
    public void consumerCreateProduct(String productEvent){
        ObjectMapper objectMapper = new ObjectMapper();
        ProductEventDTO productEventDTO;
        try {
            productEventDTO = objectMapper.readValue(productEvent, ProductEventDTO.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        switch (productEventDTO.getEventType()){
            case "CREATED" -> {
                inventoryService.createProduct(productEventDTO);
            }
            case "DELETED" -> {
                inventoryService.delete(productEventDTO.getProductVariants());
            }
        }
    }


}
