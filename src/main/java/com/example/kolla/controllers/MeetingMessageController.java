package com.example.kolla.controllers;

import com.example.kolla.dto.MessageCreateDTO;
import com.example.kolla.dto.search.MeetingMessageSearchDTO;
import com.example.kolla.responses.MeetingMessageResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.PageResponse;
import jakarta.validation.Valid;
import com.example.kolla.services.MeetingMessageService;
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

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Meeting Message Management", description = "APIs for managing meeting messages")
public class MeetingMessageController {

    private final MeetingMessageService meetingMessageService;

    @Operation(summary = "Create message")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MeetingMessageResponse>> createMessage(
            @Valid @RequestBody MessageCreateDTO createDTO) {
        MeetingMessageResponse message = meetingMessageService.createMessage(createDTO);
        return ResponseEntity.ok(ApiResponse.success("Message created successfully", message));
    }

    @Operation(summary = "Update message")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MeetingMessageResponse>> updateMessage(
            @PathVariable Long id,
            @RequestParam String content) {
        MeetingMessageResponse message = meetingMessageService.updateMessage(id, content);
        return ResponseEntity.ok(ApiResponse.success("Message updated successfully", message));
    }

    @Operation(summary = "Get message by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MeetingMessageResponse>> getMessageById(@PathVariable Long id) {
        MeetingMessageResponse message = meetingMessageService.getMessageById(id);
        return ResponseEntity.ok(ApiResponse.success("Message retrieved successfully", message));
    }

    @Operation(summary = "Get messages by meeting")
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<ApiResponse<PageResponse<MeetingMessageResponse>>> getMessagesByMeeting(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<MeetingMessageResponse> messages = meetingMessageService.getMessagesByMeeting(meetingId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Messages retrieved successfully", messages));
    }

    @Operation(summary = "Get private messages")
    @GetMapping("/meetings/{meetingId}/private/{otherUserId}")
    public ResponseEntity<ApiResponse<PageResponse<MeetingMessageResponse>>> getPrivateMessages(
            @PathVariable Long meetingId,
            @PathVariable Long otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        PageResponse<MeetingMessageResponse> messages = meetingMessageService.getPrivateMessages(
                meetingId, userId, otherUserId, page, size);
        return ResponseEntity.ok(ApiResponse.success("Private messages retrieved successfully", messages));
    }

    @GetMapping("/search")
    @Operation(summary = "Search messages with criteria", description = "Search messages with flexible criteria and pagination")
    public ResponseEntity<ApiResponse<PageResponse<MeetingMessageResponse>>> searchMessages(
            @Parameter(description = "Search keyword (message content, sender name)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Meeting ID") @RequestParam(required = false) Long meetingId,
            @Parameter(description = "Sender ID") @RequestParam(required = false) Long senderId,
            @Parameter(description = "Receiver ID (for private messages)") @RequestParam(required = false) Long receiverId,
            @Parameter(description = "Start date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (dd/MM/yyyy HH:mm hoặc dd/MM/yyyy)") @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "sentAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        MeetingMessageSearchDTO searchDTO = new MeetingMessageSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setMeetingId(meetingId);
        searchDTO.setSenderId(senderId);
        searchDTO.setReceiverId(receiverId);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        PageResponse<MeetingMessageResponse> messages = meetingMessageService.searchMessages(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @Operation(summary = "Delete message")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long id) {
        meetingMessageService.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully"));
    }

    @Operation(summary = "Delete all messages for a meeting")
    @DeleteMapping("/meetings/{meetingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteAllMessages(@PathVariable Long meetingId) {
        meetingMessageService.deleteAllMessages(meetingId);
        return ResponseEntity.ok(ApiResponse.success("All messages deleted successfully"));
    }

    @Operation(summary = "Check if message is editable")
    @GetMapping("/{id}/editable")
    public ResponseEntity<ApiResponse<Boolean>> isMessageEditable(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        boolean isEditable = meetingMessageService.isMessageEditable(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Message editability checked", isEditable));
    }
}