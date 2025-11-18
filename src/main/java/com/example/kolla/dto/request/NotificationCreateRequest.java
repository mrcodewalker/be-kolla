package com.example.kolla.dto.request;

import com.example.kolla.enums.NotificationType;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class NotificationCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    private String entityType;
    private Long entityId;
    
    @NotNull(message = "Receiver IDs are required")
    private List<Long> receiverIds;
}