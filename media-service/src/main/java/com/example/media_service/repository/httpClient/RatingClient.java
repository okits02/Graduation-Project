package com.example.media_service.repository.httpClient;

import com.okits02.common_lib.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "rating-service")
public interface RatingClient {
    @PutMapping(value = "/rating-service/rating/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> updateImageForRating(
            @RequestParam("imageUrl") List<String> imageUrl,
            @RequestParam(value = "id") String id
    );

    @PutMapping(value = "/rating-service/comment/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<?> updateImageForComment(
            @RequestParam("imageUrl") List<String> imageUrl,
            @RequestParam(value = "id") String id
    );

}
