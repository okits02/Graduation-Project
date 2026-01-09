package com.okits02.delivery_serivce.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GhtkFeeResponse {
    private boolean success;
    private String message;
    private FeeData fee;

    @Getter
    @Setter
    public static class FeeData {
        private int fee;
        private String delivery_time;
    }
}
