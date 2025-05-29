package com.example.product_service.repository.httpClient;

import com.example.product_service.configuration.FeignMultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "media-service",
        url = "http://localhost:8084",
        configuration = FeignMultipartSupportConfig.class)
public interface MediaClient {
    @PostMapping(value = "/media/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> url(@RequestHeader("Authorization") String token,
                               @RequestPart("file") MultipartFile multipartFile,
                               @RequestPart("name") String name);

    @DeleteMapping(value = "media/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<String> deleteImage(@RequestHeader("Authorization") String token,
                                       @RequestParam("url") String imgUrl);
}
