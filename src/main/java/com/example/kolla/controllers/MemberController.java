package com.example.kolla.controllers;

import com.example.kolla.dto.MeetingAccessCheckDTO;
import com.example.kolla.dto.MemberApprovalDTO;
import com.example.kolla.dto.MemberCreateDTO;
import com.example.kolla.dto.MemberInviteDTO;
import com.example.kolla.dto.MemberRejectDTO;
import com.example.kolla.dto.MemberUpdateDTO;
import com.example.kolla.dto.search.MemberSearchDTO;
import com.example.kolla.responses.*;
import com.example.kolla.utils.AuthorizationTokenService;
import com.example.kolla.services.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "APIs for managing meeting members")
public class MemberController {

    private final MemberService memberService;
    private final AuthorizationTokenService authorizationTokenService;
// @PreAuthorize("hasAnyRole('ADMIN','SECRETARY')")
    @PostMapping("/invite")
    @Operation(summary = "Invite members to meeting", description = "Invite a list of users with specified roleIds, active by default")
    public ResponseEntity<ApiResponse<Void>> inviteMembers(
            @Valid @RequestBody MemberInviteDTO inviteDTO) {
        memberService.inviteMembers(inviteDTO);
        return ResponseEntity.ok(ApiResponse.success("Members invited successfully"));
    }

    @PostMapping("/request")
    @Operation(summary = "User request to join meeting", description = "User sends request to join a meeting (isActive = false)")
    public ResponseEntity<ApiResponse<Void>> createMemberRequest(
            @Valid @RequestBody MemberCreateDTO memberCreateDTO,
            jakarta.servlet.http.HttpServletRequest request) {
        Long currentUserId = authorizationTokenService.extractUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        memberService.createMemberRequest(currentUserId, memberCreateDTO.getMeetingId(), false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member request created successfully"));
    }

    @PostMapping("/approve")
    @Operation(summary = "Approve member request", description = "Admin/Secretary approves member request and sets role")
    public ResponseEntity<ApiResponse<Void>> approveMember(
            @Valid @RequestBody MemberApprovalDTO memberApprovalDTO) {
        memberService.approveMember(memberApprovalDTO);
        return ResponseEntity.ok(ApiResponse.success("Member approved successfully"));
    }

    @PostMapping("/reject")
    @Operation(summary = "Reject member requests", description = "Admin/Secretary rejects multiple member requests (sets isActive to false)")
    public ResponseEntity<ApiResponse<Void>> rejectMembers(
            @Valid @RequestBody MemberRejectDTO rejectDTO) {
        memberService.rejectMembers(rejectDTO);
        return ResponseEntity.ok(ApiResponse.success("Member requests rejected successfully"));
    }

    @PutMapping("/{memberId}")
    @Operation(summary = "Update member", description = "Update member information (Authenticated users)")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @Parameter(description = "Member ID to update") @PathVariable Long memberId,
            @Valid @RequestBody MemberUpdateDTO memberUpdateDTO) {
        MemberResponse response = memberService.updateMember(memberId, memberUpdateDTO);
        // Không thể suy ra meetingId từ MemberResponse, bỏ tính meeting-based counts ở đây
        return ResponseEntity.ok(ApiResponse.success("Member updated successfully", response));
    }

    @DeleteMapping("/{memberId}")
    @Operation(summary = "Delete member", description = "Delete member (user self-remove or admin/secretary)")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @Parameter(description = "Member ID to delete") @PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member deleted successfully"));
    }

    @DeleteMapping("/user/{userId}/room/{roomId}")
    @Operation(summary = "Delete member by user and room", description = "Delete member by user ID and room ID")
    public ResponseEntity<ApiResponse<Void>> deleteMemberByUserAndRoom(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Room ID") @PathVariable Long roomId) {
        memberService.deleteMemberByUserAndRoom(userId, roomId);
        return ResponseEntity.ok(ApiResponse.success("Member deleted successfully"));
    }

    @GetMapping("/{memberId}")
    @Operation(summary = "Get member by ID", description = "Get member information by ID")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(
            @Parameter(description = "Member ID") @PathVariable Long memberId) {
        MemberResponse response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Search members with criteria", description = "Search members with flexible criteria and pagination (meeting-based)")
    public ResponseEntity<ApiResponse<PageResponse<MemberListResponse>>> searchMembers(
            @Parameter(description = "Search keyword (name, email, meeting title)") @RequestParam(required = false) String keyword,
            @Parameter(description = "User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Meeting ID") @RequestParam(required = false) Long meetingId,
            @Parameter(description = "Role ID") @RequestParam(required = false) Long roleId,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        MemberSearchDTO searchDTO = new MemberSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setUserId(userId);
        searchDTO.setMeetingId(meetingId);
        searchDTO.setRoleId(roleId);
        searchDTO.setIsActive(isActive);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        PageResponse<MemberListResponse> response = memberService.searchMembers(searchDTO);
        if (!response.getContent().isEmpty() && !response.getContent().get(0).getMembers().isEmpty()) {
            MemberListResponse memberList = response.getContent().get(0);

            memberList.setPendingCount(meetingId != null ? memberService.countPendingMembersByMeetingId(meetingId) : null);
            memberList.setJoinedCount(meetingId != null ? memberService.countJoinedMembersByMeetingId(meetingId) : null);
        }
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/check/{userId}/{meetingId}")
    @Operation(summary = "Check if user is member of meeting", description = "Check if user is already a member of the meeting")
    public ResponseEntity<ApiResponse<Boolean>> isUserMemberOfMeeting(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Meeting ID") @PathVariable Long meetingId) {
        boolean isMember = memberService.isUserMemberOfMeeting(userId, meetingId);
        return ResponseEntity.ok(ApiResponse.success(isMember));
    }

    @PostMapping("/check-meeting-access")
    @Operation(summary = "Check meeting access", description = "Check if meeting room exists and is accessible")
    public ResponseEntity<ApiResponse<MeetingAccessResponse>> checkMeetingAccess(
            @Valid @RequestBody MeetingAccessCheckDTO meetingAccessCheckDTO) {
        MeetingAccessResponse response = memberService.checkMeetingAccess(
            meetingAccessCheckDTO.getToken(), 
            meetingAccessCheckDTO.getMeetLink()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
