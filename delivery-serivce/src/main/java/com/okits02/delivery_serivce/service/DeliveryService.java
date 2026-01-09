package com.okits02.delivery_serivce.service;

import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;

public interface DeliveryService {
    public ShippingFeeResponse calculate(
            String toAddress,
            String toProvince,
            String toDistrict,
            String fromProvince,
            String fromDistrict,
            Integer weight,
            Long orderValue
    );
}
