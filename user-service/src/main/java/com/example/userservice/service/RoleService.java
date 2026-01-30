package com.example.userservice.service;

import com.example.userservice.model.Role;

import java.util.List;

public interface RoleService {
    public Role create(String roleName);
    public void delete(String roleName);
    public List<Role> getAll();
}
