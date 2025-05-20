package com.example.notification_service.controller;

import com.example.notification_service.dto.ApiResponse;
import com.example.notification_service.model.Notification;
import com.example.notification_service.service.NotificationConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationConsumer notificationConsumer;

    @GetMapping
    public ApiResponse<List<Notification>> getAllNotification(){
        List<Notification> notificationList = notificationConsumer.getNotificationEvent();
        return ApiResponse.<List<Notification>>builder()
                .code(200)
                .message("Successfully")
                .result(notificationList)
                .build();
    }
}
