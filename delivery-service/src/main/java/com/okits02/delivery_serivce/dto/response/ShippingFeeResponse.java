package com.okits02.delivery_serivce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShippingFeeResponse {
    private int fee;
    private String estimatedDeliveryTime;

    public static ShippingFeeResponse from(GhtkFeeResponse response) {
        return new ShippingFeeResponse(
                response.getFee().getFee(),
                response.getFee().getDelivery_time()
        );
    }
}
