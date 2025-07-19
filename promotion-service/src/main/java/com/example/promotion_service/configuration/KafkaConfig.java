package com.example.promotion_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic promotionTopic(){
        return TopicBuilder.name("promotion-event")
                .replicas(3)
                .partitions(12)
                .build();
    }

    @Bean
    public NewTopic promotionStatusTopic() {
        return TopicBuilder.name("promotion-status-event")
                .replicas(3)
                .partitions(15)
                .build();
    }
}
