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
@Table(name = "user_session")
@Data
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "device_info")
    private String deviceInfo;
    @Column(name = "ip_address", columnDefinition = "VARCHAR(100)")
    private String ipAddress;
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    @Column(name = "location", columnDefinition = "TEXT")
    private String location;
    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;
    @Column(name = "updated_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;
    @Column(name = "is_active", columnDefinition = "TINYINT(1)")
    private boolean isActive;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "action", columnDefinition = "VARCHAR(100)")
    private String action;
}