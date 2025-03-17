package com.example.userservice.mapper;

import com.example.userservice.dto.request.UserAddressCreateRequest;
import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.model.UserAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAddressMapper {
    UserAddress toUserAddress(UserAddressCreateRequest request);
}
