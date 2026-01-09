package com.example.api_gateway.configuration;

import com.okits02.common_lib.dto.ApiResponse;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationFilter implements GlobalFilter, Ordered {
    final IdentityService identityService;
    final ObjectMapper objectMapper;
    static final PathMatcher PATH_MATCHER = new AntPathMatcher();
    private final String[] publicEndpoints = {
            "/user-service/users/register",
            "/user-service/auth/introspect",
            "/user-service/auth/login",
            "/user-service/auth/verify",
            "/user-service/users/verifyEmail/send-otp",
            "/user-service/users/forgot-password/send-otp",
            "/user-service/auth/forgot-password",
            "/user-service/auth/refresh",
            "/product-service/category/getAll",
            "/search-service/search/catalog-search",
            "/search-service/search/search_suggest",
            "/search-service/search/autocomplete/quick",
            "/search-service/search/autocomplete/full",
            "/search-service/search/product",
            "/promotion-service/promotion/voucher",
            "/search-service/search/category/**",
            "/payment-service/bank/vnpay-ipn",
            "/product-service/brand/get-all",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/**/v3/api-docs"};

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
            if(!introspectResponse.getResult().isValid()) return unauthenticated(exchange.getResponse());
            if(!introspectResponse.getResult().isVerified()) return forbidden(exchange.getResponse());
            return chain.filter(exchange);
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
        String path = request.getURI().getPath();
        return Arrays.stream(publicEndpoints)
                .anyMatch(pattern ->
                        PATH_MATCHER.match(apiPrefix + pattern, path) ||
                                PATH_MATCHER.match(pattern, path)
                );
    }

    Mono<Void> unauthenticated(ServerHttpResponse response)
    {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(401)
                .message("Unauthenticated!!")
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

    Mono<Void> forbidden(ServerHttpResponse response){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(403)
                .message("User is not verified email!")
                .build();
        try {
            String body = objectMapper.writeValueAsString(apiResponse);
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(body.getBytes()))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
