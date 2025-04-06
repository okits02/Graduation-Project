package com.example.notification_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String channel;
    String recipient;
    String subject;
    @ElementCollection
    @CollectionTable(name = "notification_metadata", joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "meta_key")
    @Column(name = "meta_value")
    Map<String, String> param;
    String content;
}
