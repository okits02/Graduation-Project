package com.example.product_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "media-service", url = "http://localhost:8084")
public interface MediaClient {
    @PostMapping(value = "/media/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> url(@RequestHeader("Authorization") String token,
                               @RequestParam("file") MultipartFile multipartFile,
                               @RequestParam("name") String name);
}
