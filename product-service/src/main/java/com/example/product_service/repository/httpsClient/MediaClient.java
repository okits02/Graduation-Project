package com.example.product_service.repository.httpsClient;

import com.example.product_service.dto.request.ImageUploadRequest;
import com.example.product_service.dto.response.ListMediaResponse;
import com.example.product_service.dto.response.MediaResponse;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.product_service.enums.MediaOwnerType;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "media-service")
public interface MediaClient {
    @GetMapping(value = "/media-service/media/product/get-media", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ListMediaResponse>> getMedia(
            @RequestParam("ownerId") String ownerId,
            @RequestParam("mediaOwnerType") MediaOwnerType mediaOwnerType
    );

    @PostMapping(value = "/media-service/media/product/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<ListMediaResponse>> uploadMedia(
            @RequestParam("productId") String productId,
            @RequestParam("productImage") List<MultipartFile> productImage,
            @RequestHeader("Authorization") String token
    );

    @PostMapping(value = "/media-service/media/thumbnail",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<MediaResponse>> uploadImage(
            @ModelAttribute ImageUploadRequest request
            );
}
