package com.example.notification_service.repository.httpClient;

import com.example.notification_service.dto.response.ListEmailResponse;
import com.okits02.common_lib.dto.ApiResponse;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping(value = "/user-service/users/internals/emails", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ListEmailResponse> getListEmail(
            @RequestHeader("Authorization") String auth,
            @RequestParam("userIds") List<String> userIds
    );
}
