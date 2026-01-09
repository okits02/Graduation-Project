package com.okits02.delivery_serivce.enums;

public enum DeliveryStatus {
    CREATED,        // tạo local
    SENT_TO_GHTK,   // đã gọi API tạo đơn
    PICKED_UP,      // GHTK đã lấy hàng
    IN_TRANSIT,     // đang giao
    DELIVERED,      // giao thành công
    FAILED,         // giao thất bại
    CANCELED        // hủy
}
