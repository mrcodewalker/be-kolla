package com.example.kolla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private String fileName;
    private String fileUrl;
    private String contentType;
    private long size;
    private String bucketName;
}