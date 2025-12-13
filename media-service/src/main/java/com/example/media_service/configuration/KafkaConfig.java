package com.example.media_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic applyThumbnailUrlForProduct(){
        return TopicBuilder.name("apply-thumbnail-event")
                .partitions(3)
                .replicas(2)
                .build();
    }
}
