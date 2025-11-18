package com.example.kolla.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceLogDTO {
    private Long id;
    private UserDTO user;
    private LocalDateTime joinTime;
    private LocalDateTime leaveTime;
    private String deviceInfo;
    private String ipAddress;
    private String location;
}