package com.example.order_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {
    @Bean
    public NewTopic ordersAnalysisTopic(){return TopicBuilder.name("order-analysis-event")
            .partitions(3)
            .replicas(2)
            .build();
    }
}
