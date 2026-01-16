package com.okits02.delivery_serivce.service.Impl;

import com.okits02.delivery_serivce.dto.response.GhtkFeeResponse;
import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;
import com.okits02.delivery_serivce.model.StoreInfo;
import com.okits02.delivery_serivce.repository.StoreInfoRepository;
import com.okits02.delivery_serivce.repository.httpCient.GhtkClient;
import com.okits02.delivery_serivce.service.DeliveryService;
import com.okits02.delivery_serivce.service.StoreInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final GhtkClient ghtkClient;
    private final StoreInfoRepository storeInfoRepository;
    @Override
    public ShippingFeeResponse calculate(String toAddress, String toProvince, String toDistrict,
                                         String fromProvince, String fromDistrict, Integer weight, Long orderValue) {
        StoreInfo storeInfo = getStoreInfo();
        GhtkFeeResponse response = ghtkClient.getShippingFee(
                toAddress,
                toProvince,
                toDistrict,
                storeInfo.getPickProvince(),
                storeInfo.getPickDistrict(),
                1000,
                orderValue
        );

        if (!response.isSuccess()) {
            throw new RuntimeException("Cannot calculate shipping fee");
        }

        return ShippingFeeResponse.from(response);
    }

    private StoreInfo getStoreInfo(){
        List<StoreInfo> storeInfoList = storeInfoRepository.findAll();
        return storeInfoList.get(0);
    }
}
