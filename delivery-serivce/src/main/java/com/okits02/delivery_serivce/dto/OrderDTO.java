package com.okits02.delivery_serivce.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    String id;

    String pickName;
    String pickAddress;
    String pickProvince;
    String pickDistrict;
    String pickTel;

    String name;
    String address;
    String province;
    String district;
    String ward;
    String street;
    String tel;

    Integer pickMoney;
    Integer value;
}
