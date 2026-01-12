package com.example.product_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {
    @Bean
    public NewTopic productsTopic(){return TopicBuilder.name("product-event")
            .partitions(3)
            .replicas(1)
            .build();
    }
    @Bean
    public NewTopic categoryTopic(){return TopicBuilder.name("category-event")
            .replicas(1)
            .partitions(3)
            .build();
    }

    @Bean
    NewTopic deleteProductTopic(){
        return TopicBuilder.name("product-delete-topics")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic deleteCateTopics(){
        return TopicBuilder.name("category-event-topics")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
