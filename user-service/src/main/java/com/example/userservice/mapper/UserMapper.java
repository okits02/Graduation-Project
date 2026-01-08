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
    UserResponse toUserResponse(Users user);
}
