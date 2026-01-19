package com.okits02.delivery_serivce.service;

import com.okits02.delivery_serivce.dto.request.StoreInfoCreationRequest;
import com.okits02.delivery_serivce.dto.request.StoreInfoUpdateRequest;
import com.okits02.delivery_serivce.dto.response.StoreInfoResponse;
import com.okits02.delivery_serivce.model.StoreInfo;

import java.util.List;

public interface StoreInfoService {
    public StoreInfoResponse creation(StoreInfoCreationRequest request);
    public StoreInfoResponse update(StoreInfoUpdateRequest request);
    public void delete(String id);
    public List<StoreInfoResponse> get();
}
