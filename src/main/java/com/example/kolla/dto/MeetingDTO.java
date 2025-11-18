package com.example.kolla.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MeetingDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;
    private UserDTO createdBy;
    private boolean isRecording;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}