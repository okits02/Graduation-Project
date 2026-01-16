package com.okits02.delivery_serivce.dto.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreInfoResponse {
    String id;
    String pickName;
    String pickAddress;
    String pickProvince;
    String pickDistrict;
    String pickTell;
}
