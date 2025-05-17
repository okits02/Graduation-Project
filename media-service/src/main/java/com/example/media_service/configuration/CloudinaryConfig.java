package com.example.media_service.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {
    private final CloudinaryConfigProperties cloudinaryConfigProperties;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinaryConfigProperties.getCloudName());
        config.put("api_key", cloudinaryConfigProperties.getApiKey());
        config.put("api_secret", cloudinaryConfigProperties.getApiSecret());
        return new Cloudinary(config);
    }
}
