package com.okits02.payment_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic changeIsStockStatusEvent(){
        return TopicBuilder.name("change-status-order")
                .partitions(3)
                .replicas(2)
                .build();
    }
}
