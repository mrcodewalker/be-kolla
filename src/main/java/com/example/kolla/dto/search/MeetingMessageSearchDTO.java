package com.example.kolla.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingMessageSearchDTO {
    private String keyword; // Tìm kiếm theo message content, sender name
    private Long meetingId;
    private Long senderId;
    private Long receiverId; // Cho private messages
    private LocalDateTime startDate; // Filter từ sentAt
    private LocalDateTime endDate; // Filter đến sentAt
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
