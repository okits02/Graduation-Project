package com.okits02.delivery_serivce.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GhtkOrderData {
    @JsonProperty("partner_id")
    String partnerId;
    String label;

    String area;

    String fee;

    @JsonProperty("insurance_fee")
    String insuranceFee;

    @JsonProperty("tracking_id")
    Long trackingId;

    @JsonProperty("estimated_pick_time")
    String estimatedPickTime;

    @JsonProperty("estimated_deliver_time")
    String estimatedDeliverTime;

    List<Object> products;

    @JsonProperty("status_id")
    Integer statusId;
}
