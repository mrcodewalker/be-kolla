package com.example.kolla.models;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "attendance_log")
@Data
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_present")
    private boolean isPresent;

    @Column(name = "leave_at")
    private LocalDateTime leaveAt;

    @Column(name = "join_at")
    private LocalDateTime joinAt;
    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;
    @Column(name = "ip_address")
    private String ipAddress;
    @Column(name = "device_info")
    private String deviceInfo;
    @Column(name = "location")
    private String location;
}