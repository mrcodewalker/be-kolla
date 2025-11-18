package com.example.kolla.responses;

import com.example.kolla.models.Member;
import com.example.kolla.models.Role;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String name;
    private Long meetingId;
    private String meetingTitle;
    private String meetingCode;
    private String roleName;
    private Long roleId;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    public static MemberResponse mapToResponse(Member member) {
        User user = member.getUser();
        Role role = member.getRole();
        Meeting meeting = member.getMeeting();
        return MemberResponse.builder()
                .id(member.getId())
                .userId(user != null ? user.getId() : null)
                .name(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .meetingId(meeting != null ? meeting.getId() : null)
                .meetingTitle(meeting != null ? meeting.getTitle() : null)
                .meetingCode(meeting != null ? meeting.getMeetingCode() : null)
                .roleName(role != null ? role.getName() : null)
                .roleId(role != null ? role.getId() : null)
                .isActive(member.isActive())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
    
    public static MemberResponse mapToResponse(Member member, Long pendingCount, Long joinedCount) {
        User user = member.getUser();
        Role role = member.getRole();
        Meeting meeting = member.getMeeting();
        return MemberResponse.builder()
                .id(member.getId())
                .userId(user != null ? user.getId() : null)
                .name(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .meetingId(meeting != null ? meeting.getId() : null)
                .meetingTitle(meeting != null ? meeting.getTitle() : null)
                .meetingCode(meeting != null ? meeting.getMeetingCode() : null)
                .roleName(role != null ? role.getName() : null)
                .roleId(role != null ? role.getId() : null)
                .isActive(member.isActive())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
