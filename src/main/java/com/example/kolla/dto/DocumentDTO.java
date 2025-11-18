package com.example.kolla.dto;

import lombok.Data;
import com.example.kolla.enums.FileType;
import java.time.LocalDateTime;

@Data
public class DocumentDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private FileType fileType;
    private Long fileSize;
    private UserDTO uploadedBy;
    private LocalDateTime uploadedAt;
    private String description;
}