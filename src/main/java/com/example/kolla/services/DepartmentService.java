package com.example.kolla.services;

import com.example.kolla.dto.DepartmentDTO;
import com.example.kolla.responses.DepartmentResponse;
import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentDTO departmentDTO);
    DepartmentResponse updateDepartment(Long id, DepartmentDTO departmentDTO);
    void deleteDepartment(Long id);
    DepartmentResponse getDepartmentById(Long id);
    List<DepartmentResponse> getAllDepartments();
    boolean existsByName(String name);
    List<DepartmentResponse> searchByName(String name);
}