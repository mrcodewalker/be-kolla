package com.example.kolla.controllers;

import com.example.kolla.dto.RoomDTO;
import com.example.kolla.dto.search.RoomSearchDTO;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.RoomResponse;
import com.example.kolla.responses.ScheduleEventResponse;
import com.example.kolla.services.RoomService;
import com.example.kolla.utils.SecurityContextUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "APIs for managing rooms")
public class RoomController {

    private final RoomService roomService;
    private final SecurityContextUtil securityContextUtil;
    @Operation(summary = "Search rooms with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        RoomSearchDTO searchDTO = new RoomSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setDepartmentId(departmentId);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        PageResponse<RoomResponse> rooms = roomService.searchRooms(searchDTO);
        return ResponseEntity.ok(ApiResponse.success("Rooms retrieved successfully", rooms));
    }

    @Operation(summary = "Get room by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse room = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success("Room retrieved successfully", room));
    }

    @Operation(summary = "Create new room")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@RequestBody RoomDTO roomDTO) {
        RoomResponse createdRoom = roomService.createRoom(roomDTO);
        return ResponseEntity.ok(ApiResponse.success("Room created successfully", createdRoom));
    }

    @Operation(summary = "Update room")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        RoomResponse updatedRoom = roomService.updateRoom(id, roomDTO);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", updatedRoom));
    }

    @Operation(summary = "Delete room")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully"));
    }

    @Operation(summary = "Get user's schedule (all events from rooms they are a member of)")
    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<ScheduleEventResponse>>> getSchedule() {
        Long currentUserId = securityContextUtil.getCurrentUser().getId();
        List<ScheduleEventResponse> schedule = roomService.getSchedule(currentUserId);
        return ResponseEntity.ok(ApiResponse.success("Schedule retrieved successfully", schedule));
    }
}