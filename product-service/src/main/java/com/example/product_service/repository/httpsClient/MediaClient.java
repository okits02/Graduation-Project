package com.example.product_service.repository.httpsClient;

import com.example.product_service.dto.request.GetMediaRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.ListMediaResponse;
import com.example.product_service.enums.MediaOwnerType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "media-service")
public interface MediaClient {
    @GetMapping(value = "/media-service/media/product/get-media", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ListMediaResponse>> getMedia(
            @RequestParam("ownerId") String ownerId,
            @RequestParam("mediaOwnerType") MediaOwnerType mediaOwnerType
    );
}
