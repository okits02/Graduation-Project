package com.okits02.delivery_serivce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDTO {
    @JsonProperty("id")
    private String id;

    @JsonProperty("pick_name")
    private String pickName;

    @JsonProperty("pick_address")
    private String pickAddress;

    @JsonProperty("pick_province")
    private String pickProvince;

    @JsonProperty("pick_district")
    private String pickDistrict;
    @JsonProperty("pick_ward")
    private String pickWard;

    @JsonProperty("pick_tel")
    private String pickTel;

    @JsonProperty("name")
    private String name;

    @JsonProperty("address")
    private String address;

    @JsonProperty("province")
    private String province;

    @JsonProperty("district")
    private String district;

    @JsonProperty("ward")
    private String ward;

    @JsonProperty("hamlet")
    private String hamlet;

    @JsonProperty("tel")
    private String tel;

    @JsonProperty("value")
    private Integer value;

    @JsonProperty("pick_money")
    private Integer pickMoney;

    @JsonProperty("transport")
    private String transport;

    @JsonProperty("pick_option")
    private String pickOption;
}
