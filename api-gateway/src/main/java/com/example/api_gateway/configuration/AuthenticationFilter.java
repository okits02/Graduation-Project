package com.example.api_gateway.configuration;

import com.example.api_gateway.dto.response.ApiResponse;
import com.example.api_gateway.dto.response.IntrospectResponse;
import com.example.api_gateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    final IdentityService identityService;
    final ObjectMapper objectMapper;
    private final String[] publicEndpoints = {
            "/users/register",
            "/auth/introspect",
            "/auth/login",
            "/auth/verify",
            "/users/verifyEmail/send-otp",
            "/users/forgot-password/send-otp",
            "/auth/forgot-password",
            "/auth/refresh",
            "/product/getAll",
            "/product/*",
            "/category/getAll",
            "/search/catalog-search",
            "/search/search_suggest"};

    @Value("${app.api-prefix}")
    private String apiPrefix;
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if(isPublicEndpoint(exchange.getRequest()))
        {
            return chain.filter(exchange);
        }
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if(CollectionUtils.isEmpty(authHeader))
            return unauthenticated(exchange.getResponse());

        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        return identityService.introspect(token).flatMap(introspectResponse -> {
            log.info("Introspect API response: {}", introspectResponse);
            if(introspectResponse.getResult().isValid())
                return chain.filter(exchange);
            else
                return unauthenticated(exchange.getResponse());

        }).onErrorResume(throwable -> {
            log.error("Introspect failed: {}", throwable.getMessage(), throwable);
            return unauthenticated(exchange.getResponse());});
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request)
    {
        return Arrays.stream(publicEndpoints)
                .anyMatch(s -> request.getURI().getPath(). matches(apiPrefix + s));
    }

    Mono<Void> unauthenticated(ServerHttpResponse response)
    {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();
        String body = null;
        try{
            body = objectMapper.writeValueAsString(apiResponse);
        }catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
}
