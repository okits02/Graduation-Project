package com.example.notification_service.service;

import com.example.notification_service.model.Notification;
import com.example.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final NotificationRepository repository;
    private final EmailService emailService;

    @KafkaListener(topics = "send-to")
    public void consumerRegisterNotification(String notificationEvent) throws MessagingException, UnsupportedEncodingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Notification notification = null;
        try {
            notification = objectMapper.readValue(notificationEvent, Notification.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        emailService.sendVerificationOtpEmail(notification.getFirstName(), notification.getRecipient(), notification.getContent());
        repository.save(notification);
    }

    @KafkaListener(topics = "notification")
    public void consumerNotification(String notificationEvent) throws MessagingException, UnsupportedEncodingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Notification notification = null;
        try{
            notification = objectMapper.readValue(notificationEvent, Notification.class);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        emailService.sendUpcomingEventEmail(notification.getFirstName(), notification.getLastName(), notification.getRecipient(), notification.getContent());
        repository.save(notification);
    }

    public List<Notification> getNotificationEvent() {
        return repository.findAll();
    }
}
