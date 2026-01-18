package com.okits02.delivery_serivce.Controller;

import com.okits02.common_lib.dto.ApiResponse;
import com.okits02.delivery_serivce.dto.request.StoreInfoCreationRequest;
import com.okits02.delivery_serivce.dto.request.StoreInfoUpdateRequest;
import com.okits02.delivery_serivce.dto.response.StoreInfoResponse;
import com.okits02.delivery_serivce.service.StoreInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreInfoController {
    private final StoreInfoService service;

    @PostMapping("/create")
    public ApiResponse<StoreInfoResponse> create(
            @RequestBody StoreInfoCreationRequest request
            ){
        return ApiResponse.<StoreInfoResponse>builder()
                .code(200)
                .message("create store info successfully")
                .result(service.creation(request))
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<StoreInfoResponse> update(
            @RequestBody StoreInfoUpdateRequest request
    ){
        return ApiResponse.<StoreInfoResponse>builder()
                .code(200)
                .message("create store info successfully")
                .result(service.update(request))
                .build();
    }

    @DeleteMapping
    public ApiResponse<?> update(
            @RequestParam String id
    ){
        service.delete(id);
        return ApiResponse.builder()
                .code(200)
                .message("create store info successfully")
                .build();
    }

    @GetMapping
    public ApiResponse<?> get(
    ){
        return ApiResponse.<List<StoreInfoResponse>>builder()
                .code(200)
                .message("create store info successfully")
                .result(service.get())
                .build();
    }
}
