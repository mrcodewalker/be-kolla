package com.example.kolla.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
public class MeetingMessageDTO {
    private Long id;
    private Long meetingId;
    private String content;
    private Long senderId;
    private Long receiverId;
    private UserDTO sender;
    private LocalDateTime createdAt;
    private boolean isEdited;
    private DocumentDTO attachment;
}