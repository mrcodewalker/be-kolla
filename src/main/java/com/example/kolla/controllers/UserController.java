package com.example.kolla.controllers;

import com.example.kolla.dto.UserDTO;
import com.example.kolla.dto.search.UserSearchDTO;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.responses.UserResponse;
import com.example.kolla.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @GetMapping("/search-basic")
    @Operation(summary = "Basic search users (list)", description = "Search users by name or email (LIKE), returns a list")
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> searchBasic(
            @Parameter(description = "Keyword for name/email LIKE search") @RequestParam String q) {
        java.util.List<UserResponse> results = userService.searchByNameOrEmail(q);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getUsername()==null){
            throw new ResourceNotFoundException("Can not find user");
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByEmail(userDetails.getUsername())));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @Operation(summary = "Get user by email")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserByEmail(email)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users with criteria", description = "Search users with flexible criteria and pagination")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @Parameter(description = "Search keyword (name, email, userCode)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Name") @RequestParam(required = false) String name,
            @Parameter(description = "Email") @RequestParam(required = false) String email,
            @Parameter(description = "User code") @RequestParam(required = false) String userCode,
            @Parameter(description = "Phone number") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "Identification") @RequestParam(required = false) String identification,
            @Parameter(description = "Bank name") @RequestParam(required = false) String bankName,
            @Parameter(description = "Bank number") @RequestParam(required = false) String bankNumber,
            @Parameter(description = "Department ID") @RequestParam(required = false) Long departmentId,
            @Parameter(description = "Role ID") @RequestParam(required = false) Long roleId,
            @Parameter(description = "Degree") @RequestParam(required = false) String degree,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        UserSearchDTO searchDTO = new UserSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setName(name);
        searchDTO.setEmail(email);
        searchDTO.setUserCode(userCode);
        searchDTO.setPhoneNumber(phoneNumber);
        searchDTO.setIdentification(identification);
        searchDTO.setBankName(bankName);
        searchDTO.setBankNumber(bankNumber);
        searchDTO.setDepartmentId(departmentId);
        searchDTO.setRoleId(roleId);
        if (degree != null && !degree.isEmpty()) {
            try {
                searchDTO.setDegree(com.example.kolla.enums.Degree.valueOf(degree.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid degree values
            }
        }
        searchDTO.setIsActive(isActive);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        return ResponseEntity.ok(ApiResponse.success(userService.searchUsers(searchDTO)));
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody UserDTO updateDTO,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", userService.updateUser(id, updateDTO, request)));
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @Operation(summary = "Update user avatar")
    @PutMapping("/{id}/avatar")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @PathVariable Long id,
            @RequestParam String avatarUrl,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Avatar updated successfully", userService.updateAvatar(id, avatarUrl, request)));
    }

    @Operation(summary = "Update user password")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updatePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", userService.updatePassword(id, oldPassword, newPassword, request)));
    }

    @Operation(summary = "Update my profile", description = "Update current user's profile information. User can only update their own profile.")
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @RequestBody UserDTO updateDTO,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", userService.updateMyProfile(updateDTO, request)));
    }

    @Operation(summary = "Update my password", description = "Update current user's password. User can only update their own password.")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyPassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully", userService.updateMyPassword(oldPassword, newPassword, request)));
    }
}