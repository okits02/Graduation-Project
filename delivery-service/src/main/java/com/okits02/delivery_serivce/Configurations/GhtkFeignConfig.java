package com.okits02.delivery_serivce.Configurations;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GhtkFeignConfig {
    @Value("${ghtk.token}")
    private String token;

    @Value("${ghtk.partner-code}")
    private String partnerCode;

    @Bean
    public RequestInterceptor ghtkRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Token", token);
            requestTemplate.header("X-Client-Source", partnerCode);
        };
    }
}
