package com.example.profile_service.controller;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.response.AddressResponse;
import com.okits02.common_lib.dto.ApiResponse;
import com.example.profile_service.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/address")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddressController {
    AddressService addressService;

    @Operation(summary = "create address",
            description = "Api used to create user's address",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    ApiResponse<AddressResponse> createAddress(
            @PathVariable String userId,
            @RequestBody @Valid AddressRequest request)
    {
        return ApiResponse.<AddressResponse>builder()
                .code(200)
                .message("Create successful address!")
                .result(addressService.createAddress(request))
                .build();
    }

    @Operation(summary = "get all my address",
            description = "Api used to get all user's address",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping()
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAllMyAddress() {
        List<AddressResponse> list = addressService.getAllMyAddress();
        ApiResponse<List<AddressResponse>> response = ApiResponse.<List<AddressResponse>>builder()
                .code(200)
                .message("Lấy danh sách địa chỉ thành công")
                .result(list)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "update my address",
            description = "Api used to update user's address",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @RequestBody AddressRequest request
    ) {
        AddressResponse updated = addressService.updateMyAddress(request);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .code(200)
                .message("Cập nhật địa chỉ thành công")
                .result(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "delete my address",
            description = "Api used to delete user's address",
            security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<String>> deleteAddress(@PathVariable String addressId) {
        addressService.deleteAddress(addressId);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(200)
                .message("Xoá địa chỉ thành công")
                .result("Địa chỉ đã được xoá")
                .build();
        return ResponseEntity.ok(response);
    }
}
