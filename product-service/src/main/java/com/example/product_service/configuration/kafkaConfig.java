package com.example.product_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class kafkaConfig {
    @Bean
    public NewTopic createProductsTopic(){return TopicBuilder.name("product-create-event")
            .partitions(3)
            .replicas(2)
            .build();
    }

    @Bean
    public NewTopic updateProductsTopic(){return TopicBuilder.name("product-update-event")
            .partitions(3)
            .replicas(2)
            .build();
    }

    @Bean
    public NewTopic deleteProductsTopic(){return TopicBuilder.name("product-delete-event")
            .replicas(2)
            .partitions(3)
            .build();}

    @Bean
    public NewTopic categoryTopic(){return TopicBuilder.name("category-event")
            .replicas(2)
            .partitions(3)
            .build();
    }
}
