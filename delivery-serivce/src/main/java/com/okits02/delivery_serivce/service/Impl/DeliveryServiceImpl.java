package com.okits02.delivery_serivce.service.Impl;

import com.okits02.delivery_serivce.dto.response.GhtkFeeResponse;
import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;
import com.okits02.delivery_serivce.repository.httpCient.GhtkClient;
import com.okits02.delivery_serivce.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final GhtkClient ghtkClient;
    @Override
    public ShippingFeeResponse calculate(String toAddress, String toProvince, String toDistrict,
                                         String fromProvince, String fromDistrict, Integer weight, Long orderValue) {
        GhtkFeeResponse response = ghtkClient.getShippingFee(
                toAddress,
                toProvince,
                toDistrict,
                fromProvince,
                fromDistrict,
                weight,
                orderValue
        );

        if (!response.isSuccess()) {
            throw new RuntimeException("Cannot calculate shipping fee");
        }

        return ShippingFeeResponse.from(response);
    }
}
