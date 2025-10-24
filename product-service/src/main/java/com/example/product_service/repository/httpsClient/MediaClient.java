package com.example.product_service.repository.httpsClient;

import com.example.product_service.configuration.MediaClientFallbackFactory;
import com.example.product_service.dto.response.ListMediaResponse;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.product_service.enums.MediaOwnerType;
import com.okits02.common_lib.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "media-service",
        configuration = FeignConfig.class,
        fallbackFactory = MediaClientFallbackFactory.class)
public interface MediaClient {
    @GetMapping(value = "/media-service/media/product/get-media", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ListMediaResponse>> getMedia(
            @RequestParam("ownerId") String ownerId,
            @RequestParam("mediaOwnerType") MediaOwnerType mediaOwnerType
    );
}
