package com.example.kolla.dto;

import com.example.kolla.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private NotificationType notificationType;
    private Long senderId;
    private UserDTO sender;
    private Long receiverId;
    private Long meetingId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
}