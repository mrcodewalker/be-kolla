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
@Table(name = "recording")
@Data
public class Recording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;
    @Column(name = "start_time", columnDefinition = "DATETIME(6)")
    private LocalDateTime startTime;
    @Column(name = "end_time", columnDefinition = "DATETIME(6)")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;
    @Column(name = "file_name", columnDefinition = "VARCHAR(255)")
    private String fileName;
    @Column(name = "file_size", columnDefinition = "BIGINT")
    private Long fileSize;
    @Column(name = "file_content", columnDefinition = "LONGBLOB")
    private byte[] fileContent; 
    @Column(name = "created_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;
}
