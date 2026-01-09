package com.okits02.delivery_serivce.repository.httpCient;

import com.okits02.delivery_serivce.Configurations.GhtkFeignConfig;
import com.okits02.delivery_serivce.dto.response.GhtkFeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
}
