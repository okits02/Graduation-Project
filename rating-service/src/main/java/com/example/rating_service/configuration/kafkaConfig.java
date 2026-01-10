package com.example.rating_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {
    @Bean
    public NewTopic productsTopic(){return TopicBuilder.name("rating-event")
            .partitions(3)
            .replicas(2)
            .build();
    }
}
