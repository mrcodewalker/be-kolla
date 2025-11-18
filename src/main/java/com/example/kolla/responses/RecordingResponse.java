package com.example.kolla.responses;

import com.example.kolla.models.Recording;
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
public class RecordingResponse {
    private Long id;
    private String url;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String createdByName;
    private String createdByEmail;
    private String meetingTitle;
    private String meetingCode;
    private String fileName;
    private Long fileSize;
    private LocalDateTime createdAt;
    
    public static RecordingResponse mapToResponse(Recording recording) {
        User createdBy = recording.getCreatedBy();
        Meeting meeting = recording.getMeeting();
        
        return RecordingResponse.builder()
                .id(recording.getId())
                .url(recording.getUrl())
                .startTime(recording.getStartTime())
                .endTime(recording.getEndTime())
                .createdByName(createdBy != null ? createdBy.getName() : null)
                .createdByEmail(createdBy != null ? createdBy.getEmail() : null)
                .meetingTitle(meeting != null ? meeting.getTitle() : null)
                .meetingCode(meeting != null ? meeting.getMeetingCode() : null)
                .fileName(recording.getFileName())
                .fileSize(recording.getFileSize())
                .createdAt(recording.getCreatedAt())
                .build();
    }
}
