package com.example.product_service.configuration;

import com.example.product_service.dto.RemoveCategoryRequest;
import com.example.product_service.dto.response.ListMediaResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.example.product_service.repository.httpsClient.MediaClient;
import com.example.product_service.repository.httpsClient.SearchClient;
import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.common_lib.exception.AppException;
import com.okits02.common_lib.exception.GlobalErrorCode;
import com.okits02.common_lib.feign.BaseFallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchClientFallbackFactory extends BaseFallbackFactory<SearchClient> {
    @Override
    protected SearchClient createFallbackInstance(Throwable cause) {
        return new SearchClient() {
            @Override
            public ResponseEntity<ApiResponse<Long>> removeCate(RemoveCategoryRequest request) {
                throw new AppException(GlobalErrorCode.INTERNAL_ERROR);
            }
        };
    }

    @Override
    protected String getClientName() {
        return "search-service";
    }


}
