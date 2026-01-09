package com.okits02.delivery_serivce.repository.httpCient;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.delivery_serivce.dto.response.AddressResponse;
import com.okits02.delivery_serivce.dto.response.ProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "profile-service")
public interface ProfileClient {
    @GetMapping(value = "/profile-service/profile/internal/delivery",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProfileResponse> getProfileForDelivery(
            @RequestParam(value = "userId") String userId);

    @GetMapping(value = "/profile-service/address/internal/delivery",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<AddressResponse> getAddressForDelivery(
            @RequestParam(value = "addressId") String addressId
    );
}
