package com.example.kolla.repositories;

import com.example.kolla.models.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByDepartmentName(String name);
    List<Department> findByDepartmentNameContainingIgnoreCase(String name);
    boolean existsByDepartmentCode(String departmentCode);
}