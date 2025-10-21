package com.okits02.common_lib.feign;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public ErrorDecoder errorDecoder(){
        return new FeignErrorDecoder();
    }

    @Bean
    public RequestInterceptor feignRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.header("X-Request-Id", java.util.UUID.randomUUID().toString());
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

}
