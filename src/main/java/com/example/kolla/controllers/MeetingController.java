package com.example.kolla.controllers;

import com.example.kolla.dto.MeetingCreateDTO;
import com.example.kolla.dto.search.MeetingSearchDTO;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.MeetingChartStatsResponse;
import com.example.kolla.responses.MeetingResponse;
import com.example.kolla.responses.MemberMeetingStatsResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.UserResponse;
import com.example.kolla.services.MeetingService;
import com.example.kolla.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting Management", description = "APIs for managing meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final UserService userService;
    private final com.example.kolla.services.MemberService memberService;

    @Operation(summary = "Create new meeting")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MeetingCreateDTO meetingCreateDTO) {
        UserResponse user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Meeting created successfully", meetingService.createMeeting(user.getId(), meetingCreateDTO)));
    }

    @Operation(summary = "Get meeting by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeetingById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getUserByEmail(userDetails.getUsername());
        // Chỉ cho phép nếu user đã được approve vào meeting
        boolean allowed = memberService.isUserMemberOfMeeting(user.getId(), id);
        if (!allowed) {
            return ResponseEntity.status(403).body(ApiResponse.error("You don't have access to this meeting"));
        }
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMeetingById(id)));
    }
    @Operation(summary = "Get all meetings by Room ID with membership status")
    @GetMapping("/room/{id}")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getMeetingByRoomId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status) {
        UserResponse user = userService.getUserByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMeetingByRoomIdWithStatus(id, user.getId(), status)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search meetings with criteria", description = "Search meetings with flexible criteria and pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<MeetingResponse>>> searchMeetings(
            @Parameter(description = "Search keyword (title, description)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Title") @RequestParam(required = false) String title,
            @Parameter(description = "Description") @RequestParam(required = false) String description,
            @Parameter(description = "Created by user ID") @RequestParam(required = false) Long createdBy,
            @Parameter(description = "Room ID") @RequestParam(required = false) Long roomId,
            @Parameter(description = "Start time (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "End time (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "Start date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Is recording") @RequestParam(required = false) Boolean isRecording,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        MeetingSearchDTO searchDTO = new MeetingSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setTitle(title);
        searchDTO.setDescription(description);
        searchDTO.setCreatedBy(createdBy);
        searchDTO.setRoomId(roomId);
        searchDTO.setStartTime(startTime);
        searchDTO.setEndTime(endTime);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setIsRecording(isRecording);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        return ResponseEntity.ok(ApiResponse.success(meetingService.searchMeetings(searchDTO)));
    }

    @Operation(summary = "Get meeting counts per member for charting", description = "Returns total meetings per active member within an optional time range")
    @GetMapping("/stats/members")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<List<MemberMeetingStatsResponse>>> getMemberMeetingStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMemberMeetingStats(startDate, endDate)));
    }

    @Operation(summary = "Get daily meeting stats for charting",
            description = "Provides totals of meetings and active participants per day within the requested window (ideal for line/area charts). "
                    + "If no date is supplied, defaults to the last 7 days. You can also pass days=7,14,... for quick presets.")
    @GetMapping("/stats/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<MeetingChartStatsResponse>> getDailyMeetingStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate endDate,
            @RequestParam(required = false) Integer days) {

        if (days != null) {
            if (days < 1) {
                throw new BadRequestException("days must be greater than or equal to 1");
            }
            LocalDate resolvedEnd = endDate != null
                    ? endDate
                    : (startDate != null ? startDate : LocalDate.now());
            LocalDate resolvedStart = resolvedEnd.minusDays(days - 1);
            startDate = resolvedStart;
            endDate = resolvedEnd;
        }

        return ResponseEntity.ok(ApiResponse.success(meetingService.getDailyMeetingChartStats(startDate, endDate)));
    }

    @Operation(summary = "Update meeting")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @PathVariable Long id,
            @RequestBody MeetingCreateDTO meetingUpdateDTO) {
        return ResponseEntity.ok(ApiResponse.success("Meeting updated successfully", meetingService.updateMeeting(id, meetingUpdateDTO)));
    }

    @Operation(summary = "Delete meeting")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.ok(ApiResponse.success("Meeting deleted successfully"));
    }

    @Operation(summary = "Update isMeeting status to true")
    @PutMapping("/{id}/is-meeting")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateIsMeetingToTrue(@PathVariable Long id) {
        meetingService.updateIsMeetingToTrue(id);
        return ResponseEntity.ok(ApiResponse.success("Meeting status updated successfully"));
    }
}