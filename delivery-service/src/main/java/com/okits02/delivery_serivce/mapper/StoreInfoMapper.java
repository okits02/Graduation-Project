package com.okits02.delivery_serivce.mapper;

import com.okits02.delivery_serivce.dto.request.StoreInfoCreationRequest;
import com.okits02.delivery_serivce.dto.request.StoreInfoUpdateRequest;
import com.okits02.delivery_serivce.dto.response.StoreInfoResponse;
import com.okits02.delivery_serivce.model.StoreInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.lang.annotation.Target;

@Mapper(componentModel = "spring")
public interface StoreInfoMapper {
    StoreInfo toStoreInfo(StoreInfoCreationRequest request);
    StoreInfoResponse toResponse (StoreInfo storeInfo);
    void update(@MappingTarget StoreInfo storeInfo, StoreInfoUpdateRequest request);
}
