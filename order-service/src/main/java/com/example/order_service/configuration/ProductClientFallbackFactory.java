package com.example.order_service.configuration;

import com.example.order_service.dto.ProductGetVM;
import com.example.order_service.repository.httpClient.ProductClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductClientFallbackFactory extends BaseFallbackFactory<ProductClient> {
    @Override
    protected String getClientName() {
        return "product-service";
    }

    @Override
    protected ProductClient createFallbackInstance(Throwable cause) {
        return new ProductClient() {

            @Override
            public ResponseEntity<ApiResponse<ProductGetVM>> getProductDetails(String token, String productId) {
                log.warn("Fallback: cannot delete profile for user {} because {}", productId, cause.getMessage());
                throw new AppException(GlobalErrorCode.SERVICE_UNAVAILABLE);
            }
        };
    }
}
