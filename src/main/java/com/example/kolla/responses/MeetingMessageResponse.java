package com.example.kolla.responses;

import com.example.kolla.models.MeetingMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingMessageResponse {
    private Long id;
    private String message;
    private LocalDateTime sentAt;
    private Long senderId;
    private Long meetingId;

    public static MeetingMessageResponse mapToResponse(MeetingMessage message) {
        return MeetingMessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .sentAt(message.getSentAt())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .meetingId(message.getMeeting() != null ? message.getMeeting().getId() : null)
                .build();
    }
}