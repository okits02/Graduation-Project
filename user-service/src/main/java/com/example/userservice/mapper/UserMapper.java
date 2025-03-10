package com.example.userservice.mapper;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.model.Users;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Users toUser(UserCreationRequest request);
}
