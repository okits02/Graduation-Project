package com.example.rating_service.configuration;

import jakarta.persistence.Entity;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.swing.text.html.Option;
import java.util.Optional;

@Configuration
@EnableJpaRepositories("com.example.rating_service.repository")
@EntityScan("com.example.rating_service.model")
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class DatabaseAutoConfig {

    @Bean
    public AuditorAware<String> auditorAware(){
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null){
                return Optional.of(" ");
            }
            return Optional.of(authentication.getName());
        };
    }
}
