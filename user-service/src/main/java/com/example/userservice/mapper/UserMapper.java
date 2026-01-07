package com.example.userservice.mapper;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.response.UserForAdminResponse;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.model.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreationRequest request);

    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "dob", ignore = true)
    @Mapping(target = "address", ignore = true)
    UserResponse toUserResponse(Users user);

    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "dob", ignore = true)
    @Mapping(target = "address", ignore = true)
    UserForAdminResponse toUserForAdminResponse(Users user);
}
