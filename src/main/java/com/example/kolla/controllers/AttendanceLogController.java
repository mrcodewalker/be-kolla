package com.example.kolla.controllers;

import com.example.kolla.dto.AttendanceLogCreateDTO;
import com.example.kolla.dto.AttendanceLogUpdateDTO;
import com.example.kolla.dto.search.AttendanceLogSearchDTO;
import com.example.kolla.responses.AttendanceLogResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.services.AttendanceLogService;
import com.example.kolla.utils.AuthorizationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-logs")
@RequiredArgsConstructor
@Tag(name = "Attendance Log Management", description = "APIs for managing meeting attendance logs")
public class AttendanceLogController {

    private final AttendanceLogService attendanceLogService;
    private final AuthorizationTokenService authorizationTokenService;

    @Operation(summary = "Create attendance log")
    @PostMapping("/meetings/{meetingId}")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> createAttendanceLog(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        AttendanceLogResponse log = attendanceLogService.createAttendanceLog(meetingId, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Attendance log created successfully", log));
    }

    @Operation(summary = "Update leave time with token")
    @PostMapping("/{id}/leave-with-token")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> updateLeaveTimeWithToken(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceLogUpdateDTO updateDTO) {
        AttendanceLogResponse log = attendanceLogService.updateLeaveTimeWithToken(id, updateDTO.getToken());
        return ResponseEntity.ok(ApiResponse.success("Leave time updated successfully", log));
    }

    @Operation(summary = "Get attendance log by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> getAttendanceLogById(@PathVariable Long id) {
        AttendanceLogResponse log = attendanceLogService.getAttendanceLogById(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance log retrieved successfully", log));
    }

    @Operation(summary = "Get attendance logs by meeting")
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<ApiResponse<List<AttendanceLogResponse>>> getAttendanceLogsByMeeting(
            @PathVariable Long meetingId) {
        List<AttendanceLogResponse> logs = attendanceLogService.getAttendanceLogsByMeeting(meetingId);
        return ResponseEntity.ok(ApiResponse.success("Attendance logs retrieved successfully", logs));
    }

    @Operation(summary = "Get current user's attendance logs (paginated, searchable)")
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceLogResponse>>> getAttendanceLogsByUser(
            HttpServletRequest request,
            @Parameter(description = "Filter by IP address") @RequestParam(required = false) String ip,
            @Parameter(description = "Start date (dd/MM/yyyy hoặc dd/MM/yyyy HH:mm)") @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (dd/MM/yyyy hoặc dd/MM/yyyy HH:mm)") @RequestParam(required = false) String endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "joinAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

        Long extractedUserId = authorizationTokenService.extractUserId(request);

        java.time.format.DateTimeFormatter formatterDateTime = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        java.time.format.DateTimeFormatter formatterDate = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        java.time.LocalDateTime start = null;
        java.time.LocalDateTime end = null;
        try {
            if (startDate != null && !startDate.isBlank()) {
                if (startDate.trim().length() > 10) {
                    start = java.time.LocalDateTime.parse(startDate.trim(), formatterDateTime);
                } else {
                    start = java.time.LocalDate.parse(startDate.trim(), formatterDate).atStartOfDay();
                }
            }
            if (endDate != null && !endDate.isBlank()) {
                if (endDate.trim().length() > 10) {
                    end = java.time.LocalDateTime.parse(endDate.trim(), formatterDateTime);
                } else {
                    end = java.time.LocalDate.parse(endDate.trim(), formatterDate).atTime(23, 59, 59);
                }
            }
        } catch (Exception ignored) {
            // Invalid date inputs will be treated as null filters
        }

        com.example.kolla.dto.search.AttendanceLogSearchDTO searchDTO = new com.example.kolla.dto.search.AttendanceLogSearchDTO();
        searchDTO.setUserId(extractedUserId);
        // If specific IP provided, use it as keyword to match ipAddress
        if (ip != null && !ip.isBlank()) {
            searchDTO.setKeyword(ip.trim());
        }
        searchDTO.setStartDate(start);
        searchDTO.setEndDate(end);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        PageResponse<AttendanceLogResponse> logs = attendanceLogService.searchAttendanceLogs(searchDTO);
        return ResponseEntity.ok(ApiResponse.success("Attendance logs retrieved successfully", logs));
    }

    @GetMapping("/search")
    @Operation(summary = "Search attendance logs with criteria", description = "Search attendance logs with flexible criteria and pagination")
    public ResponseEntity<ApiResponse<PageResponse<AttendanceLogResponse>>> searchAttendanceLogs(
            @Parameter(description = "Search keyword (deviceInfo, ipAddress, location, userName, meetingTitle)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Meeting ID") @RequestParam(required = false) Long meetingId,
            @Parameter(description = "User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Start date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Present status") @RequestParam(required = false) Boolean isPresent,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "joinAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        AttendanceLogSearchDTO searchDTO = new AttendanceLogSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setMeetingId(meetingId);
        searchDTO.setUserId(userId);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setIsPresent(isPresent);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        PageResponse<AttendanceLogResponse> logs = attendanceLogService.searchAttendanceLogs(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @Operation(summary = "Delete attendance log")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttendanceLog(@PathVariable Long id) {
        attendanceLogService.deleteAttendanceLog(id);
        return ResponseEntity.ok(ApiResponse.success("Attendance log deleted successfully"));
    }

    @Operation(summary = "Calculate attendance rate")
    @GetMapping("/meetings/{meetingId}/users/{userId}/rate")
    public ResponseEntity<ApiResponse<Double>> calculateAttendanceRate(
            @PathVariable Long meetingId,
            @PathVariable Long userId) {
        double rate = attendanceLogService.calculateAttendanceRate(meetingId, userId);
        return ResponseEntity.ok(ApiResponse.success("Attendance rate calculated successfully", rate));
    }

    @Operation(summary = "Create attendance log with token")
    @PostMapping("/create-with-token")
    public ResponseEntity<ApiResponse<AttendanceLogResponse>> createAttendanceLogWithToken(
            @Valid @RequestBody AttendanceLogCreateDTO createDTO,
            HttpServletRequest request) {
        AttendanceLogResponse log = attendanceLogService.createAttendanceLogWithToken(
            createDTO.getToken(), 
            createDTO.getMeetLink(),
            createDTO.getAction(),
            request
        );
        return ResponseEntity.ok(ApiResponse.success("Attendance log created successfully", log));
    }
}