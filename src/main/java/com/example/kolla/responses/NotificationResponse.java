package com.example.kolla.responses;

import com.example.kolla.models.Notification;
import com.example.kolla.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private String notificationType;
    private String senderName;
    private String senderEmail;
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private Long meetingId;
    private MeetingResponse meetingResponse;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static NotificationResponse mapToResponse(Notification notification) {
        User sender = notification.getSender();
        User receiver = notification.getReceiver();
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .notificationType(notification.getType() != null ? notification.getType().toString() : null)
                .senderName(sender != null ? sender.getName() : null)
                .senderEmail(sender != null ? sender.getEmail() : null)
                .receiverId(receiver != null ? receiver.getId() : null)
                .receiverName(receiver != null ? receiver.getName() : null)
                .receiverEmail(receiver != null ? receiver.getEmail() : null)
                .meetingId(notification.getMeeting() != null ? notification.getMeeting().getId() : null)
                .meetingResponse(notification.getMeeting() != null ? MeetingResponse.mapToResponse(notification.getMeeting()) : null)
                .isRead(notification.getIsRead() != null ? notification.getIsRead() : false)
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
