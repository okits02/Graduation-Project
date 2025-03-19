package com.example.userservice.controller;

import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.request.UserAddressUpdateRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.dto.response.UserAddressResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.AppException;
import com.example.userservice.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/address")
@Controller
public class UserAddressController {
    private final UserAddressService userAddressService;

    @PostMapping("/{userId}/addresses")
    public ResponseEntity<ApiResponse<?>> addUserAddress(@PathVariable String userId,
                                                         @RequestBody UserAddressCreateRequest userAddressCreateRequest) {
        try {
            userAddressService.addUserAddressToUser(userId, userAddressCreateRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                    .code(201)
                    .message("Address added successfully")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @PutMapping("/addresses")
    public ResponseEntity<ApiResponse<?>> updateMyAddress(@RequestBody @Valid UserAddressUpdateRequest request) {
        try {
            userAddressService.updateMyAddress(request);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("Address updated successfully")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/deleteMyAddress/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteMyAddress(@PathVariable("addressId") String addressId) {
        try{
            userAddressService.deleteMyAddress(addressId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("Address deleted successfully")
                    .build());
        }catch (AppException e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @DeleteMapping("/deleteAddress/{addressId}/{userId}")
    public ResponseEntity<ApiResponse<?>> deleteAddressFromUser(@PathVariable("addressId") String addressId,
                                                                @PathVariable("userId") String userId) {
        try {
            userAddressService.deleteAddressFromUser(addressId, userId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(200)
                    .message("Address deleted successfully")
                    .build());
        } catch (AppException e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .code(e.getErrorCode().getCode())
                    .message(e.getMessage())
                    .build());
        }
    }

    @GetMapping("/user-addresses")
    public ResponseEntity<ApiResponse<Page<UserAddressResponse>>> getUserAddresses(
            @RequestParam("userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.<Page<UserAddressResponse>>builder()
                    .code(400)
                    .message("Page index must be non-negative and size must be greater than zero")
                    .build());
        }

        Page<UserAddressResponse> userAddresses = userAddressService.getAllUserAddresses(userId, page, size);

        ApiResponse<Page<UserAddressResponse>> apiResponse = ApiResponse.<Page<UserAddressResponse>>builder()
                .code(1000)
                .message("Success")
                .result(userAddresses)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
