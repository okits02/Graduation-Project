package com.example.profile_service.mapper;

import com.example.profile_service.dto.request.AddressRequest;
import com.example.profile_service.dto.response.AddressResponse;
import com.example.profile_service.entity.UserAddress;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    UserAddress toAddress(AddressRequest request);
    AddressResponse toAddressResponse(UserAddress address);
    List<AddressResponse> toListAddress(List<UserAddress> addressList);
    void updateAddress(@MappingTarget UserAddress userAddress, AddressRequest request);
}
