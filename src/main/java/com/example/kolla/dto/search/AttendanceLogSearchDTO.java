package com.example.kolla.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLogSearchDTO {
    private String keyword; // Tìm kiếm theo deviceInfo, ipAddress, location, userName, meetingTitle
    private Long meetingId;
    private Long userId;
    private LocalDateTime startDate; // Filter từ joinAt
    private LocalDateTime endDate; // Filter đến joinAt
    private Boolean isPresent;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;
}
