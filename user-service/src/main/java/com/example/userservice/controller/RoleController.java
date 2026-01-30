package com.example.userservice.controller;

import com.example.userservice.model.Role;
import com.example.userservice.service.RoleService;
import com.okits02.common_lib.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> create(@RequestParam(value = "roleName") String roleName){
        return ApiResponse.<Role>builder()
                .code(200)
                .message("create role success")
                .result(roleService.create(roleName))
                .build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@RequestParam(value = "roleName") String roleName){
        roleService.delete(roleName);
        return ApiResponse.builder()
                .code(200)
                .message("delete role success")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Role>> getAll(){
        return ApiResponse.<List<Role>>builder()
                .code(200)
                .message("get all roles")
                .result(roleService.getAll())
                .build();
    }
}
