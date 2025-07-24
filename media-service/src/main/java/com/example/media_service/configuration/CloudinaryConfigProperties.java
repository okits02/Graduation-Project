package com.example.media_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloudinary")
@Getter
@Setter
public class CloudinaryConfigProperties {
    private String cloudName;
    private String apiKey;
    private String apiSecret;
}
