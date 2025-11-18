package com.example.kolla.services;

import com.example.kolla.dto.FileDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface GoogleDriveService {
    /**
     * Upload file to Google Drive with hierarchical folder structure:
     * Level 1: Department Name
     * Level 2: Meeting Title
     * Level 3: Files
     */
    FileDTO uploadFile(Long meetingId, MultipartFile file);
    
    /**
     * Download file from Google Drive by meetingId and fileName
     */
    InputStream downloadFile(Long meetingId, String fileName);
    
    /**
     * Delete file from Google Drive by meetingId and fileName
     */
    void deleteFile(Long meetingId, String fileName);
    
    /**
     * Get file URL from Google Drive by meetingId and fileName
     */
    String getFileUrl(Long meetingId, String fileName);
    
    /**
     * Get or create folder ID for department (Level 1)
     */
    String getOrCreateDepartmentFolder(String departmentName);
    
    /**
     * Get or create folder ID for meeting (Level 2) under department folder
     */
    String getOrCreateMeetingFolder(String departmentFolderId, String meetingTitle);
}
