package com.okits02.payment_service.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(
        name = "vnpayRefundClient",
        url = "${vnpay.refund-url}"
)
public interface VnPayRefundClient {
    @PostMapping(
            value = "/merchant_webapi/api/transaction",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Map<String, String> refund(@RequestBody Map<String, String> body);
}
