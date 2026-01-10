package com.example.api_gateway.configuration;

import com.example.api_gateway.dto.request.IntrospectRequest;
import com.example.api_gateway.repository.IdentityClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okits02.common_lib.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailVerifiedFilter implements GlobalFilter, Ordered {
    final IdentityClient identityClient;
    final ObjectMapper objectMapper;

    static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private final String[] verifiedIgnoreEndpoints = {
        "/user-service/users/myInfo", "/user-service/users/forgot-password"
    };
    @Value("${app.api-prefix}")
    private String apiPrefix;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info(">>> EmailVerifiedFilter START, path={}",
                exchange.getRequest().getURI().getPath());
        Boolean authenticate = exchange.getAttribute("AUTHENTICATED");
        log.info("AUTHENTICATED = {}", authenticate);
        if(authenticate == null || !authenticate){
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        if(isIgnoredEndpoint(path)){
            return chain.filter(exchange);
        }
        String token = exchange.getAttribute("ACCESS_TOKEN");
        log.info("token = {}", token);
        if (token == null) {
            return forbidden(exchange.getResponse());
        }
        return identityClient.verified(
                        IntrospectRequest.builder()
                                .token(token)
                                .build()
                )
                .flatMap(response -> {
                    log.info("verified API response: {}", response);
                    if (!response.getResult().isVerified()) {
                        return emailNotVerified(exchange.getResponse());
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> emailNotVerified(exchange.getResponse()));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean isIgnoredEndpoint(String path){
        return Arrays.stream(verifiedIgnoreEndpoints)
                .anyMatch(pattern -> PATH_MATCHER.match(apiPrefix + pattern, path) ||
                        PATH_MATCHER.match(pattern, path));
    }
    private Mono<Void> emailNotVerified(ServerHttpResponse response){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1403)
                .message("Email is not verified!")
                .build();
        try {
            String body = objectMapper.writeValueAsString(apiResponse);
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(body.getBytes()))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private Mono<Void> forbidden(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
}
