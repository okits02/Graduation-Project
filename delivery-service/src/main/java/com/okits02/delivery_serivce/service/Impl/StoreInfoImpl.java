package com.okits02.delivery_serivce.service.Impl;

import com.okits02.common_lib.exception.AppException;
import com.okits02.delivery_serivce.dto.request.StoreInfoCreationRequest;
import com.okits02.delivery_serivce.dto.request.StoreInfoUpdateRequest;
import com.okits02.delivery_serivce.dto.response.StoreInfoResponse;
import com.okits02.delivery_serivce.exceptions.DeliveryErrorCode;
import com.okits02.delivery_serivce.mapper.StoreInfoMapper;
import com.okits02.delivery_serivce.model.StoreInfo;
import com.okits02.delivery_serivce.repository.StoreInfoRepository;
import com.okits02.delivery_serivce.service.StoreInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreInfoImpl implements StoreInfoService {
    private final StoreInfoRepository storeInfoRepository;
    private final StoreInfoMapper storeInfoMapper;
    @Override
    public StoreInfoResponse creation(StoreInfoCreationRequest request) {
        StoreInfo storeInfo = storeInfoMapper.toStoreInfo(request);
        return storeInfoMapper.toResponse(storeInfoRepository.save(storeInfo));
    }

    @Override
    public StoreInfoResponse update(StoreInfoUpdateRequest request) {
        StoreInfo storeInfo = storeInfoRepository.findById(request.getId()).orElseThrow(()
                -> new AppException(DeliveryErrorCode.INFO_NOT_EXISTS));
        storeInfoMapper.update(storeInfo, request);
        return storeInfoMapper.toResponse(storeInfo);
    }

    @Override
    public void delete(String id) {
        StoreInfo storeInfo = storeInfoRepository.findById(id).orElseThrow(()
                -> new AppException(DeliveryErrorCode.INFO_NOT_EXISTS));
        storeInfoRepository.delete(storeInfo);
    }

    @Override
    public List<StoreInfoResponse> get() {
        List<StoreInfo> storeInfos = storeInfoRepository.findAll();
        List<StoreInfoResponse> list = storeInfos.stream().map(storeInfoMapper::toResponse).toList();
        return list;
    }
}
