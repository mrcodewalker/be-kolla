package com.example.kolla.responses;

import com.example.kolla.models.Meeting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEventResponse {
    private Long id;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String roomName;
    private Long roomId;
    private String meetingCode;

    public static ScheduleEventResponse fromMeeting(Meeting meeting) {
        return ScheduleEventResponse.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .roomName(meeting.getRoom().getRoomName())
                .roomId(meeting.getRoom().getId())
                .meetingCode(meeting.getMeetingCode())
                .build();
    }
}
