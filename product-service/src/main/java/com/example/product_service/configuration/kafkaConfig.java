package com.example.product_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {
    @Bean
    public NewTopic createProductsTopic(){return TopicBuilder.name("create-product")
            .partitions(3)
            .replicas(1)
            .build();
    }
}
