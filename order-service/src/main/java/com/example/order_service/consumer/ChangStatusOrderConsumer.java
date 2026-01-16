package com.example.order_service.consumer;

import com.example.order_service.dto.ChangeStatusOrderDTO;
import com.example.order_service.enums.Status;
import com.example.order_service.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangStatusOrderConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "change-status-order",
            containerFactory = "changeStatusOrderKafkaListenerContainerFactory")
    public void consumerChangeStatusOrders(String changStatusEvent){
        ObjectMapper objectMapper = new ObjectMapper();
        ChangeStatusOrderDTO changeStatusOrderDTO;
        try {
            changeStatusOrderDTO = objectMapper.readValue(changStatusEvent, ChangeStatusOrderDTO.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        switch (changeStatusOrderDTO.getStatus()){
            case SUCCESS -> orderService.changStatusOrderForPayment(changeStatusOrderDTO.getPaymentId(),
                    changeStatusOrderDTO.getOrderId(), Status.PROCESSING);
            case CANCELLED, EXPIRED, FAILED -> orderService.changStatusOrderForPayment(changeStatusOrderDTO.getPaymentId(),
                    changeStatusOrderDTO.getOrderId(), Status.PENDING);
        }
    }
}
