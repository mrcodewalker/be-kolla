package com.example.kolla.services;

import com.example.kolla.dto.search.AttendanceLogSearchDTO;
import com.example.kolla.responses.AttendanceLogResponse;
import com.example.kolla.responses.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceLogService {
    AttendanceLogResponse createAttendanceLog(Long meetingId, Long userId, HttpServletRequest request);
    AttendanceLogResponse updateLeaveTime(Long logId, LocalDateTime leaveTime);
    AttendanceLogResponse updateLeaveTimeWithToken(Long logId, String token);
    AttendanceLogResponse getAttendanceLogById(Long id);
    List<AttendanceLogResponse> getAttendanceLogsByMeeting(Long meetingId);
    List<AttendanceLogResponse> getAttendanceLogsByUser(Long userId);
    PageResponse<AttendanceLogResponse> searchAttendanceLogs(AttendanceLogSearchDTO searchDTO);
    void deleteAttendanceLog(Long id);
    double calculateAttendanceRate(Long meetingId, Long userId);
    AttendanceLogResponse createAttendanceLogWithToken(String token, String meetLink, String action, HttpServletRequest request);
}