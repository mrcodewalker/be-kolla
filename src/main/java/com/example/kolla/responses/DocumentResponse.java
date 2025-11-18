package com.example.kolla.responses;

import com.example.kolla.models.Document;
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
public class DocumentResponse {
    private Long id;
    private String fileName;
    private Long fileSize;
    private LocalDateTime createdAt;
    private String fileType;
    private LocalDateTime updatedAt;
    private String userName;
    private String userEmail;
    private String meetingTitle;
    private String meetingCode;
    
    public static DocumentResponse mapToResponse(Document document) {
        User user = document.getUser();
        Meeting meeting = document.getMeeting();
        
        return DocumentResponse.builder()
                .id(document.getId())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .createdAt(document.getCreatedAt())
                .fileType(document.getFileType())
                .updatedAt(document.getUpdatedAt())
                .userName(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .meetingTitle(meeting != null ? meeting.getTitle() : null)
                .meetingCode(meeting != null ? meeting.getMeetingCode() : null)
                .build();
    }
}
