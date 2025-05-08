package com.example.userservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic registerTopic()
    {
        return TopicBuilder.name("send-otp").build();
    }

    @Bean
    public NewTopic createProfileTopic(){return TopicBuilder.name("create-profile")
            .partitions(3)
            .replicas(1)
            .build(); }

    @Bean
    public NewTopic createAddressTopic(){return TopicBuilder.name("create-address")
            .partitions(3)
            .replicas(1)
            .build(); }
}
