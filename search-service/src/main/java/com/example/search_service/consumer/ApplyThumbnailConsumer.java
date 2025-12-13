package com.example.search_service.consumer;

import com.example.search_service.service.CategoryService;
import com.example.search_service.service.ProductService;
import com.example.search_service.viewmodel.dto.ApplyThumbnailEventDTO;
import com.example.search_service.viewmodel.dto.request.ApplyThumbnailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplyThumbnailConsumer {
    private final ProductService productService;
    private final CategoryService categoryService;

    @KafkaListener(topics = "product-apply-thumbnail-event",
            containerFactory = "applyThumbnailKafkaListenerContainerFactory")
    public void consumerApplyThumbnail(String applyThumbnailEvent) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ApplyThumbnailEventDTO applyThumbnailEventDTO = null;
        applyThumbnailEventDTO = objectMapper.readValue(applyThumbnailEvent, ApplyThumbnailEventDTO.class);
        switch (applyThumbnailEventDTO.getMediaOwnerType()){
            case "PRODUCT" -> productService.AppyThumbnailToProduct(ApplyThumbnailRequest.builder()
                    .ownerId(applyThumbnailEventDTO.getOwnerId())
                    .url(applyThumbnailEventDTO.getUrl())
                    .build());
            case "CATEGORY" -> categoryService.applyThumbnailToCategory(ApplyThumbnailRequest.builder()
                            .ownerId(applyThumbnailEventDTO.getOwnerId())
                            .url(applyThumbnailEventDTO.getUrl())
                    .build());
        }

    }
}
