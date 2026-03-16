package com.example.notification_service.service;

import com.example.notification_service.model.Notification;
import com.example.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

//    public Notification save(Notification notification) {
//
//    }

}
