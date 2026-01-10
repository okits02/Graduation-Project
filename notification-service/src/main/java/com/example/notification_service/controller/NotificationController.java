package com.example.notification_service.controller;

import com.example.notification_service.dto.request.SendEmailRequest;
import com.example.notification_service.service.EmailService;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.notification_service.model.Notification;
import com.example.notification_service.service.NotificationConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationConsumer notificationConsumer;
    private final EmailService emailService;
    @GetMapping
    public ApiResponse<List<Notification>> getAllNotification(){
        List<Notification> notificationList = notificationConsumer.getNotificationEvent();
        return ApiResponse.<List<Notification>>builder()
                .code(200)
                .message("Successfully")
                .result(notificationList)
                .build();
    }

    @PostMapping("/marketing")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> sendEmailForUsers(
            @RequestBody SendEmailRequest request
            ){
        emailService.sendMarketingEmailToTopBuyers(request);
        return ApiResponse.builder()
                .code(200)
                .message("send email successfully!")
                .build();
    }
}
