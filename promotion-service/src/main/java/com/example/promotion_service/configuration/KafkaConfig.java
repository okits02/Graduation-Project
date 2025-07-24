package com.example.promotion_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic promotionTopic(){
        return TopicBuilder.name("promotion-create-event")
                .replicas(3)
                .partitions(12)
                .build();
    }

    @Bean
    public NewTopic promotionUpdateTopic() {
        return TopicBuilder.name("promotion-update-event")
                .replicas(3)
                .partitions(10)
                .build();
    }

    @Bean
    public NewTopic promotionDeleteTopic(){
        return TopicBuilder.name("promotion-delete-event")
                .replicas(3)
                .partitions(5)
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
