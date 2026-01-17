package com.okits02.analys_service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okits02.analys_service.viewmodel.dto.StockInAnalysisEvent;
import com.okits02.analys_service.service.StockInAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StockInConsumer {
    private final StockInAnalysisService stockInAnalysisService;

    @KafkaListener(topics = "stockIn-analysis-event",
            containerFactory = "stockInAnalysisKafkaListenerContainerFactory")
    public void consumerStockIn(String stockInEvent) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        StockInAnalysisEvent stockInAnalysisEvent;
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try {
            stockInAnalysisEvent = objectMapper.readValue(stockInEvent, StockInAnalysisEvent.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        switch (stockInAnalysisEvent.getEventType()){
            case CREATE -> stockInAnalysisService.create(stockInAnalysisEvent);
            case DELETE -> stockInAnalysisService.delete(stockInAnalysisEvent.getId());
        }
    }
}
