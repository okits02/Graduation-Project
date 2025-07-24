package com.example.userservice.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI OpenAPI(
            @Value("${spring.application.name}") String title,
            @Value("${open.api.version}") String version,
            @Value("${open.api.serverUrl}") String serverUrl,
            @Value("${open.api.serverName}") String serverName
    )
    {
        return new OpenAPI().info(new Info()
                .title(title)
                .description("API for user's Identity")
                .version(version)
                .license(new License().name("Apache 2.0")))
                .servers(List.of(new Server().url(serverUrl).description(serverName)))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

}
