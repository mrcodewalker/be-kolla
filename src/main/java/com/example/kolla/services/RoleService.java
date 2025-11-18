package com.example.kolla.services;

import com.example.kolla.dto.RoleDTO;
import com.example.kolla.responses.RoleResponse;
import java.util.List;

public interface RoleService {
    RoleResponse createRole(RoleDTO roleDTO);
    RoleResponse updateRole(Long id, RoleDTO roleDTO);
    void deleteRole(Long id);
    RoleResponse getRoleById(Long id);
    RoleResponse getRoleByName(String name);
    List<RoleResponse> getAllRoles();
    boolean existsByName(String name);
}