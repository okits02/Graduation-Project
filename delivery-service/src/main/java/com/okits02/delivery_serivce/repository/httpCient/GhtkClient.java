package com.okits02.delivery_serivce.repository.httpCient;

import com.okits02.delivery_serivce.Configurations.GhtkFeignConfig;
import com.okits02.delivery_serivce.dto.GhtkCreateOrderRequest;
import com.okits02.delivery_serivce.dto.response.GhtkFeeResponse;
import com.okits02.delivery_serivce.dto.response.GhtkOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "ghtk-client",
        url = "${ghtk.base-url}",
        configuration = GhtkFeignConfig.class
)
public interface GhtkClient {
    @GetMapping("/services/shipment/fee")
    GhtkFeeResponse getShippingFee(
            @RequestParam("address") String address,
            @RequestParam("province") String province,
            @RequestParam("district") String district,
            @RequestParam("pick_province") String pickProvince,
            @RequestParam("pick_district") String pickDistrict,
            @RequestParam("weight") Integer weight,
            @RequestParam("value") Long value
    );

    @PostMapping(value = "/services/shipment/order", produces = MediaType.APPLICATION_JSON_VALUE)
    GhtkOrderResponse createOrder(
            @RequestHeader("Token") String token,
            @RequestHeader("X-Client-Source") String clientSource,
            @RequestBody GhtkCreateOrderRequest request
    );


}
