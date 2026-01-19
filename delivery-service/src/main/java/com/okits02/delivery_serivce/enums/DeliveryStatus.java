package com.okits02.delivery_serivce.enums;

public enum DeliveryStatus {
    CREATED,        // tạo local
    IN_TRANSIT,     // đang giao
    DELIVERED,      // giao thành công
    FAILED,         // giao thất bại
    CANCELED        // hủy
}
