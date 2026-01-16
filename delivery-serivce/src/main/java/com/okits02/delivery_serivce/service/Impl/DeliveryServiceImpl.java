package com.okits02.delivery_serivce.service.Impl;

import com.okits02.delivery_serivce.dto.GhtkCreateOrderRequest;
import com.okits02.delivery_serivce.dto.OrderDTO;
import com.okits02.delivery_serivce.dto.ProductDTO;
import com.okits02.delivery_serivce.dto.request.OrderRequest;
import com.okits02.delivery_serivce.dto.response.AddressResponse;
import com.okits02.delivery_serivce.dto.response.GhtkFeeResponse;
import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;
import com.okits02.delivery_serivce.model.StoreInfo;
import com.okits02.delivery_serivce.repository.StoreInfoRepository;
import com.okits02.delivery_serivce.repository.httpCient.GhtkClient;
import com.okits02.delivery_serivce.repository.httpCient.ProfileClient;
import com.okits02.delivery_serivce.service.DeliveryService;
import com.okits02.delivery_serivce.service.StoreInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.NonFinal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final GhtkClient ghtkClient;
    private final StoreInfoRepository storeInfoRepository;
    private final ProfileClient profileClient;


    @NonFinal
    @Value("${ghtk.token}")
    private String ghtkToken;

    @NonFinal
    @Value("${ghtk.partner-code}")
    private String ghtkPartnerCode;

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

    @Override
    public Object createDeleteShipment(OrderRequest orderRequest) {
        var addressUser = profileClient.getAddressForDelivery(orderRequest.getAddressId());

        return null;
    }

    private GhtkCreateOrderRequest ghtkCreateOrderRequest(OrderRequest orderRequest, AddressResponse addressUser){
        StoreInfo storeInfo = getStoreInfo();
        if(orderRequest == null || addressUser == null) return GhtkCreateOrderRequest.builder().build();
        List<ProductDTO> productDTOs = orderRequest.getItems().stream()
                .map(item -> ProductDTO
                        .builder()
                        .name(item.getVariantName())
                        .quantity(item.getQuantity())
                        .weight(2.0)
                        .build())
                        .toList();
        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderRequest.getOrderId())
                .pickName(storeInfo.getPickAddress())
                .pickAddress(storeInfo.getPickAddress())
                .pickProvince(storeInfo.getPickProvince())
                .pickDistrict(storeInfo.getPickDistrict())
                .pickTel(storeInfo.getPickTell())
                .name(addressUser.getReceiverName())
                .address(addressUser.getAddressLine())
                .province(addressUser.getCity())
                .district(addressUser.getDistrict())
                .ward(addressUser.getWard())
                .street(addressUser.getStreet())
                .tel(addressUser.getReceiverPhone())
                .value(orderRequest.getTotalCost().intValue())
                .build();

        return GhtkCreateOrderRequest.builder()
                .orderDTO(orderDTO)
                .productDTOList(productDTOs)
                .build();
    }

    private StoreInfo getStoreInfo(){
        List<StoreInfo> storeInfoList = storeInfoRepository.findAll();
        return storeInfoList.get(0);
    }
}
