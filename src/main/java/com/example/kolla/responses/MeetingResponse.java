package com.example.kolla.responses;

import com.example.kolla.models.Meeting;
import com.example.kolla.models.User;
import com.example.kolla.models.Room;
import com.example.kolla.models.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingResponse {
    private Long id;
    private String title;
    private String description;
    private String meetingCode;
    private String meetingLink;
    private String createdByName;
    private String createdByEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomName;
    private String roomCode;
    private String departmentName;
    private String departmentCode;
    private String membershipStatus; // APPROVED, PENDING, REJECTED
    private String meetingRole; // Role of the requesting user in this meeting
    private Boolean isComingSoon; // true if current time < startTime
    private Boolean isExpired; // true if current time > endTime
    private Boolean isMeeting; // true if meeting is active
    
    public static MeetingResponse mapToResponse(Meeting meeting, String membershipStatus) {
        return mapToResponse(meeting, membershipStatus, null);
    }

    public static MeetingResponse mapToResponse(Meeting meeting, String membershipStatus, String meetingRole) {
        User createdBy = meeting.getCreatedBy();
        Room room = meeting.getRoom();
        Department department = room.getDepartment();
        
        LocalDateTime now = LocalDateTime.now();
        boolean isComingSoon = meeting.getStartTime() != null && now.isBefore(meeting.getStartTime());
        boolean isExpired = meeting.getEndTime() != null && now.isAfter(meeting.getEndTime());
        
        return MeetingResponse.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .meetingCode(meeting.getMeetingCode())
                .meetingLink(meeting.getMeetingLink())
                .createdByName(createdBy != null ? createdBy.getName() : null)
                .createdByEmail(createdBy != null ? createdBy.getEmail() : null)
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .roomName(room != null ? room.getRoomName() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .departmentName(department != null ? department.getDepartmentName() : null)
                .departmentCode(department != null ? department.getDepartmentCode() : null)
                .membershipStatus(membershipStatus)
                .meetingRole(meetingRole)
                .isComingSoon(isComingSoon)
                .isExpired(isExpired)
                .isMeeting(meeting.isMeeting())
                .build();
    }
    
    public static MeetingResponse mapToResponse(Meeting meeting) {
        return mapToResponse(meeting, null, null);
    }
}
