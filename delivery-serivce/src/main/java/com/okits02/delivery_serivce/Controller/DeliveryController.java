package com.okits02.delivery_serivce.Controller;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;
import com.okits02.delivery_serivce.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService shippingFeeService;

    @GetMapping("/shipping-fee")
    public ApiResponse<ShippingFeeResponse> getShippingFee(
            @RequestParam String toAddress,
            @RequestParam String toProvince,
            @RequestParam String toDistrict,
            @RequestParam String fromProvince,
            @RequestParam String fromDistrict,
            @RequestParam Integer weight,
            @RequestParam Long orderValue
    ) {
        return ApiResponse.<ShippingFeeResponse>builder()
                .result(shippingFeeService.calculate(
                        toAddress,
                        toProvince,
                        toDistrict,
                        fromProvince,
                        fromDistrict,
                        weight,
                        orderValue
                ))
                .build();
    }
}
