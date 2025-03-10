package com.example.userservice.controller;

import com.example.userservice.dto.request.UserCreationRequest;
import com.example.userservice.dto.response.ApiResponse;
import com.example.userservice.model.Users;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequestMapping("/users")
@RequiredArgsConstructor
@Controller
public class UserController {
    private UserService userService;

    @PostMapping
    ApiResponse<Users> creationUser(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<Users> apiResponse = new ApiResponse<>();
        apiResponse.setCode(1000);
        apiResponse.setMessage("success");
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }
}
