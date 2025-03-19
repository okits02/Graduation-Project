package com.example.userservice.mapper;

import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.request.UserAddressUpdateRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.model.UserAddress;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {
    UserAddress toUserAddress(UserAddressCreateRequest request);
    void updateUserAddressFormRequest(UserAddressUpdateRequest request, @MappingTarget UserAddress userAddress);
}
