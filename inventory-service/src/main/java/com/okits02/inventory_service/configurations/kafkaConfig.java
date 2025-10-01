package com.okits02.inventory_service.configurations;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {
    @Bean
    public NewTopic changeIsStockStatusEvent(){
        return TopicBuilder.name("change-status-event")
                .partitions(3)
                .replicas(2)
                .build();
    }
}
