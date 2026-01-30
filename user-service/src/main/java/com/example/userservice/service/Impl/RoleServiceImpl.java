package com.example.userservice.service.Impl;

import com.example.userservice.exception.UserErrorCode;
import com.example.userservice.model.Role;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.service.RoleService;
import com.okits02.common_lib.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public Role create(String roleName) {
        if(roleRepository.existsByName(roleName)){
            throw new AppException(UserErrorCode.ROLE_EXISTS);
        }
        Role role = Role.builder()
                .name(roleName)
                .build();
        return roleRepository.save(role);
    }

    @Override
    public void delete(String roleName) {
        if(!roleRepository.existsByName(roleName)){
            throw new AppException(UserErrorCode.ROLE_NOT_EXISTS);
        }
        roleRepository.deleteById(roleName);
    }

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }
}
