package com.example.kolla.services;
import com.example.kolla.dto.MeetingCreateDTO;
import com.example.kolla.dto.search.MeetingSearchDTO;
import com.example.kolla.models.Meeting;
import com.example.kolla.responses.MeetingChartStatsResponse;
import com.example.kolla.responses.MeetingResponse;
import com.example.kolla.responses.MemberMeetingStatsResponse;
import com.example.kolla.responses.PageResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MeetingService {
    MeetingResponse createMeeting(Long userId, MeetingCreateDTO createDTO);
    MeetingResponse updateMeeting(Long id, MeetingCreateDTO updateDTO);
    MeetingResponse getMeetingById(Long id);
    List<MeetingResponse> getMeetingByRoomId(Long roomId);
    List<MeetingResponse> getMeetingByRoomIdWithStatus(Long roomId, Long userId, String status);
    Meeting findById(Long id);
    PageResponse<MeetingResponse> searchMeetings(MeetingSearchDTO searchDTO);
    List<MeetingResponse> getMeetingsPreviewByUser(Long userId);
    List<MemberMeetingStatsResponse> getMemberMeetingStats(LocalDateTime startDate, LocalDateTime endDate);
    MeetingChartStatsResponse getDailyMeetingChartStats(LocalDate startDate, LocalDate endDate);
    void deleteMeeting(Long id);
    void validateMeetingTime(LocalDateTime startTime, LocalDateTime endTime);
    void updateIsMeetingToTrue(Long meetingId);
}