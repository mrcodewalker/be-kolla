package com.example.kolla.services.impl;

import com.example.kolla.dto.DepartmentDTO;
import com.example.kolla.responses.DepartmentResponse;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.Department;
import com.example.kolla.repositories.DepartmentRepository;
import com.example.kolla.services.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public DepartmentResponse createDepartment(DepartmentDTO departmentDTO) {
        if (departmentRepository.existsByDepartmentName(departmentDTO.getName())) {
            throw new BadRequestException("Department with this name already exists");
        }
        Department department = new Department();
        department.setDepartmentName(departmentDTO.getName());
        department.setDepartmentCode(generateDepartmentCode());
        department = departmentRepository.save(department);
        return DepartmentResponse.mapToResponse(department);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentDTO departmentDTO) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        
        if (!department.getDepartmentName().equals(departmentDTO.getName()) &&
            departmentRepository.existsByDepartmentName(departmentDTO.getName())) {
            throw new BadRequestException("Department with this name already exists");
        }

        if (departmentDTO.getName() != null) {
            department.setDepartmentName(departmentDTO.getName());
        }
        department = departmentRepository.save(department);
        return DepartmentResponse.mapToResponse(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        return DepartmentResponse.mapToResponse(department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return departmentRepository.existsByDepartmentName(name);
    }

    @Override
    public List<DepartmentResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        return departmentRepository.findByDepartmentNameContainingIgnoreCase(name.trim()).stream()
                .map(DepartmentResponse::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateDepartmentCode() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        final SecureRandom random = new SecureRandom();

        String code;
        do {
            StringBuilder builder = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                builder.append(chars.charAt(random.nextInt(chars.length())));
            }
            code = builder.toString();
        } while (departmentRepository.existsByDepartmentCode(code));

        return code;
    }
}