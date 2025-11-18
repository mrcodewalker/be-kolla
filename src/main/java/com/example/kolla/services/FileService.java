package com.example.kolla.services;

import com.example.kolla.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {
    FileDTO uploadFile(String bucketName, MultipartFile file);
    InputStream downloadFile(String bucketName, String fileName);
    void deleteFile(String bucketName, String fileName);
    String getFileUrl(String bucketName, String fileName);
    boolean createBucket(String bucketName);
}