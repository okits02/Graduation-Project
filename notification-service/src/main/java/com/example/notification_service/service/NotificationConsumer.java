package com.example.notification_service.service;

import com.example.notification_service.dto.NotificationEvent;
import com.example.notification_service.model.Notification;
import com.example.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@EnableKafka
public class NotificationConsumer {
    private final NotificationRepository repository;
    private final EmailService emailService;

    @KafkaListener(topics = "send-otp")
    public void consumerRegisterNotification(String notificationEvent) throws MessagingException, UnsupportedEncodingException {
        log.info("🔔 [send-to] Received raw event: {}", notificationEvent);
        ObjectMapper objectMapper = new ObjectMapper();
        Notification notification = null;
        try {
            notification = objectMapper.readValue(notificationEvent, Notification.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        emailService.sendVerificationOtpEmail(notification.getRecipient(), notification.getContent());
        repository.save(notification);
    }

    @KafkaListener(topics = "order-notification-event",
            containerFactory = "orderNotificationKafkaListenerContainerFactory")
    public void consumerNotification(String notificationEvent) throws MessagingException, UnsupportedEncodingException {
        log.info("🔔 [send-to] Received raw event: {}", notificationEvent);
        ObjectMapper objectMapper = new ObjectMapper();
        NotificationEvent OrderNotificationEvent = null;
        try{
            OrderNotificationEvent = objectMapper.readValue(notificationEvent, NotificationEvent.class);
        } catch (JsonMappingException e) {
            log.error("❌ Failed to process notification: {}", notificationEvent, e);
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to process notification: {}", notificationEvent, e);
        }
        emailService.sendEmailForOrder(OrderNotificationEvent);
    }

    public List<Notification> getNotificationEvent() {
        return repository.findAll();
    }
}
