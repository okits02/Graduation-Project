package com.example.search_service.repository.httpClient;

import com.example.search_service.enums.MediaOwnerType;
import com.example.search_service.viewmodel.dto.response.ListMediaResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "media-service")
public interface MediaClient {
    @GetMapping(value = "/media-service/media/product/get-media", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ListMediaResponse>> getMedia(
            @RequestParam("ownerId") String ownerId,
            @RequestParam("mediaOwnerType") MediaOwnerType mediaOwnerType
    );
}
