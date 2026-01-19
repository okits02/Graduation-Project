package com.okits02.delivery_serivce.service.Impl;

import com.okits02.common_lib.exception.AppException;
import com.okits02.delivery_serivce.dto.GhtkCreateOrderRequest;
import com.okits02.delivery_serivce.dto.OrderDTO;
import com.okits02.delivery_serivce.dto.ProductDTO;
import com.okits02.delivery_serivce.dto.request.OrderRequest;
import com.okits02.delivery_serivce.dto.response.AddressResponse;
import com.okits02.delivery_serivce.dto.response.GhtkFeeResponse;
import com.okits02.delivery_serivce.dto.response.GhtkOrderResponse;
import com.okits02.delivery_serivce.dto.response.ShippingFeeResponse;
import com.okits02.delivery_serivce.enums.DeliveryStatus;
import com.okits02.delivery_serivce.enums.Status;
import com.okits02.delivery_serivce.exceptions.DeliveryErrorCode;
import com.okits02.delivery_serivce.model.Delivery;
import com.okits02.delivery_serivce.model.StoreInfo;
import com.okits02.delivery_serivce.repository.DeliveryRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final GhtkClient ghtkClient;
    private final StoreInfoRepository storeInfoRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProfileClient profileClient;


    @NonFinal
    @Value("${ghtk.token}")
    private String ghtkToken;

    @NonFinal
    @Value("${ghtk.partner-code}")
    private String ghtkPartnerCode;

    @Override
    public ShippingFeeResponse calculate(String toAddress, String toProvince, String toDistrict, Long orderValue) {
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
        if(addressUser == null || addressUser.getCode() != 200) {
            throw new AppException(DeliveryErrorCode.PROFILE_NOT_EXISTS);
        }
        GhtkCreateOrderRequest ghtkCreateOrderRequest = ghtkCreateOrderRequest(orderRequest, addressUser.getResult());
        GhtkOrderResponse ghtkOrderResponse = ghtkClient.createOrder(ghtkToken, ghtkPartnerCode, ghtkCreateOrderRequest);
        DeliveryStatus status = null;
        if(ghtkOrderResponse.isSuccess()){
            status = DeliveryStatus.CREATED;
        }else {
            status = DeliveryStatus.FAILED;
        }
        Delivery delivery = Delivery.builder()
                .userId(orderRequest.getUserId())
                .orderId(orderRequest.getOrderId())
                .addressId(orderRequest.getAddressId())
                .status(status)
                .receiverName(addressUser.getResult().getReceiverName())
                .receiverPhone(addressUser.getResult().getReceiverPhone())
                .receiverAddress(addressUser.getResult().getAddressLine())
                .expectedDeliveryTime(ghtkOrderResponse.getOrder().getEstimatedDeliverTime())
                .shippingFee(BigDecimal.valueOf(orderRequest.getOrderFee()))
                .createdAt(LocalDateTime.now())
                .build();
        deliveryRepository.save(delivery);
        return ghtkOrderResponse;
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
        int gthh = orderRequest.getTotalCost() == null ? 0 : orderRequest.getTotalCost().intValue();
        if (gthh < 1) gthh = 1;
        if (gthh > 20_000_000) gthh = 20_000_000;
        String pickName = storeInfo.getPickName();
        if (pickName == null || pickName.isBlank()) {
            throw new IllegalStateException("Store pickName is missing");
        }
        String userFullAddress = String.join(", ",
                addressUser.getAddressLine(),
                addressUser.getStreet(),
                addressUser.getWard(),
                addressUser.getDistrict(),
                addressUser.getCity()
        );
        String pickFullAddress = String.join(", ",
                storeInfo.getPickAddress(),
                storeInfo.getPickWar(),
                storeInfo.getPickDistrict(),
                storeInfo.getPickProvince()
        );
        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderRequest.getOrderId())

                // PICK
                .pickName(storeInfo.getPickName())
                .pickAddress(pickFullAddress)
                .pickProvince(storeInfo.getPickProvince())
                .pickDistrict(storeInfo.getPickDistrict())
                .pickWard(storeInfo.getPickWar())
                .pickTel(storeInfo.getPickTell())

                // RECEIVE
                .name(addressUser.getReceiverName())
                .address(userFullAddress)
                .province(addressUser.getCity())
                .district(addressUser.getDistrict())
                .ward(addressUser.getWard())
                .hamlet("Khác")   // ⭐ BẮT BUỘC

                .tel(addressUser.getReceiverPhone())
                .value(gthh)
                .pickMoney(50000)
                .transport("fly")
                .pickOption("cod")
                .build();

        return GhtkCreateOrderRequest.builder()
                .order(orderDTO)
                .products(productDTOs)
                .build();
    }

    private StoreInfo getStoreInfo(){
        List<StoreInfo> storeInfoList = storeInfoRepository.findAll();
        return storeInfoList.get(0);
    }
}
