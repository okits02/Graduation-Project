package com.okits02.delivery_serivce.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StoreInfoCreationRequest {
    String pickName;
    String pickAddress;
    String pickProvince;
    String pickDistrict;
    String pickTell;
}
