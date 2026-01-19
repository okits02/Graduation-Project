package com.okits02.delivery_serivce.Controller;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.delivery_serivce.dto.request.OrderRequest;
import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;
import com.okits02.delivery_serivce.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam Long orderValue
    ) {
        return ApiResponse.<ShippingFeeResponse>builder()
                .result(shippingFeeService.calculate(
                        toAddress,
                        toProvince,
                        toDistrict,
                        orderValue
                ))
                .build();
    }

    @PostMapping("/create")
    public ApiResponse<Object> createShipment(
            @RequestBody OrderRequest orderRequest
            ){
        return ApiResponse.builder()
                .code(200)
                .result(shippingFeeService.createDeleteShipment(orderRequest))
                .build();
    }
}
