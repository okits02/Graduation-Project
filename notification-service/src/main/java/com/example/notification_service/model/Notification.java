package com.example.notification_service.model;

import com.example.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "notifications")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String userId;
    String channel;
    NotificationType type;
    String referenceId;
    String recipient;
    String subject;
    boolean isRead;
    LocalDateTime readAt;
    Map<String, String> param;
    String content;
}
