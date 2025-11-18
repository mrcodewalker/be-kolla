package com.example.kolla.responses;

import com.example.kolla.models.AttendanceLog;
import com.example.kolla.models.User;
import com.example.kolla.models.Meeting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AttendanceLogResponse {
    private Long id;
    private String userName;
    private String userEmail;
    private boolean isPresent;
    private LocalDateTime leaveAt;
    private LocalDateTime joinAt;
    private String ipAddress;
    private String deviceInfo;
    private String location;
    private MeetingResponse meeting;
    
    public static AttendanceLogResponse mapToResponse(AttendanceLog attendanceLog) {
        User user = attendanceLog.getUser();
        Meeting meeting = attendanceLog.getMeeting();
        
        return AttendanceLogResponse.builder()
                .id(attendanceLog.getId())
                .userName(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .isPresent(attendanceLog.isPresent())
                .leaveAt(attendanceLog.getLeaveAt())
                .joinAt(attendanceLog.getJoinAt())
                .ipAddress(attendanceLog.getIpAddress())
                .deviceInfo(attendanceLog.getDeviceInfo())
                .location(attendanceLog.getLocation())
                .meeting(meeting != null ? MeetingResponse.mapToResponse(meeting) : null)
                .build();
    }
}
