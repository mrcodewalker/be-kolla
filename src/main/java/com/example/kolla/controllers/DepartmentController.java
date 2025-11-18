package com.example.kolla.controllers;

import com.example.kolla.dto.DepartmentDTO;
import com.example.kolla.responses.DepartmentResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.services.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "Create new department")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@RequestBody DepartmentDTO departmentDTO) {
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", 
            departmentService.createDepartment(departmentDTO)));
    }

    @Operation(summary = "Update department")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @RequestBody DepartmentDTO departmentDTO) {
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", 
            departmentService.updateDepartment(id, departmentDTO)));
    }

    @Operation(summary = "Delete department")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully"));
    }

    @Operation(summary = "Get department by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentById(id)));
    }

    @Operation(summary = "Get all departments")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments()));
    }

    @Operation(summary = "Search departments by name")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> searchDepartmentsByName(
            @RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.searchByName(name)));
    }
}