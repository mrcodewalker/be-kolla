package com.example.kolla.services.impl;

import com.example.kolla.dto.RoleDTO;
import com.example.kolla.responses.RoleResponse;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.Role;
import com.example.kolla.repositories.RoleRepository;
import com.example.kolla.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleResponse createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByName(roleDTO.getName())) {
            throw new BadRequestException("Role with this name already exists");
        }
        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role = roleRepository.save(role);
        return RoleResponse.mapToResponse(role);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        
        if (!role.getName().equals(roleDTO.getName()) && 
            roleRepository.existsByName(roleDTO.getName())) {
            throw new BadRequestException("Role with this name already exists");
        }

        if (roleDTO.getName() != null) {
            role.setName(roleDTO.getName());
        }
        if (roleDTO.getDescription() != null) {
            role.setDescription(roleDTO.getDescription());
        }
        
        role = roleRepository.save(role);
        return RoleResponse.mapToResponse(role);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found");
        }
        roleRepository.deleteById(id);
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        return RoleResponse.mapToResponse(role);
    }

    @Override
    public RoleResponse getRoleByName(String name) {
        Role role = roleRepository.findByName(name);
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }
        return RoleResponse.mapToResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }
}