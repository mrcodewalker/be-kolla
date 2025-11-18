package com.example.kolla.controllers;

import com.example.kolla.dto.search.UserSessionSearchDTO;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.UserSessionResponse;
import com.example.kolla.services.UserSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user-sessions")
@RequiredArgsConstructor
@Tag(name = "User Session Management", description = "APIs for managing user session logs")
public class UserSessionController {

    private final UserSessionService userSessionService;

    @Operation(summary = "Get user session by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSessionResponse>> getUserSessionById(@PathVariable Long id) {
        UserSessionResponse session = userSessionService.getUserSessionById(id);
        return ResponseEntity.ok(ApiResponse.success("User session retrieved successfully", session));
    }

    @Operation(summary = "Get user sessions by user ID")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> getUserSessionsByUserId(
            @PathVariable Long userId) {
        List<UserSessionResponse> sessions = userSessionService.getUserSessionsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User sessions retrieved successfully", sessions));
    }

    @GetMapping("/search")
    @Operation(summary = "Search user sessions with criteria", description = "Search user sessions with flexible criteria and pagination")
    public ResponseEntity<ApiResponse<PageResponse<UserSessionResponse>>> searchUserSessions(
            @Parameter(description = "Search keyword (deviceInfo, ipAddress, location, action, userName, userEmail)") @RequestParam(required = false) String keyword,
            @Parameter(description = "User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Device info") @RequestParam(required = false) String deviceInfo,
            @Parameter(description = "IP address") @RequestParam(required = false) String ipAddress,
            @Parameter(description = "Location") @RequestParam(required = false) String location,
            @Parameter(description = "Action") @RequestParam(required = false) String action,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Start date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        UserSessionSearchDTO searchDTO = new UserSessionSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setUserId(userId);
        searchDTO.setDeviceInfo(deviceInfo);
        searchDTO.setIpAddress(ipAddress);
        searchDTO.setLocation(location);
        searchDTO.setAction(action);
        searchDTO.setIsActive(isActive);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        PageResponse<UserSessionResponse> sessions = userSessionService.searchUserSessions(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(summary = "Delete user session")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUserSession(@PathVariable Long id) {
        userSessionService.deleteUserSession(id);
        return ResponseEntity.ok(ApiResponse.success("User session deleted successfully"));
    }
}


