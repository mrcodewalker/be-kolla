package com.example.kolla.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
public class RecordingDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private UserDTO recordedBy;
    private LocalDateTime createdAt;
}