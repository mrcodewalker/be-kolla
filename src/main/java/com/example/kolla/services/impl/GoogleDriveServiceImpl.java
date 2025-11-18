package com.example.kolla.services.impl;

import com.example.kolla.dto.FileDTO;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.Department;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.Room;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.services.GoogleDriveService;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleDriveServiceImpl implements GoogleDriveService {

    private final Drive googleDriveClient;
    private final MeetingRepository meetingRepository;

    @Override
    public FileDTO uploadFile(Long meetingId, MultipartFile file) {
        try {
            // Get meeting information
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

            // Get department from meeting -> room -> department
            Room room = meeting.getRoom();
            if (room == null || room.getDepartment() == null) {
                throw new BadRequestException("Meeting does not have an associated department");
            }

            Department department = room.getDepartment();
            String departmentName = department.getDepartmentName() != null 
                    ? department.getDepartmentName() 
                    : department.getDepartmentCode();

            // Create hierarchical folder structure
            // Level 1: Department folder
            String departmentFolderId = getOrCreateDepartmentFolder(departmentName);
            
            // Level 2: Meeting folder (under department)
            String meetingFolderId = getOrCreateMeetingFolder(departmentFolderId, meeting.getTitle());

            // Upload file to meeting folder (Level 3)
            File fileMetadata = new File();
            String originalFileName = file.getOriginalFilename();
            String fileName = System.currentTimeMillis() + "_" + originalFileName;
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(meetingFolderId));

            // Upload file content
            java.io.File tempFile = java.io.File.createTempFile("upload_", "_" + fileName);
            try {
                file.transferTo(tempFile);
                
                FileContent mediaContent = new FileContent(file.getContentType(), tempFile);
                File uploadedFile = googleDriveClient.files().create(fileMetadata, mediaContent)
                        .setFields("id, name, webViewLink, size, mimeType")
                        .execute();

                // Get file URL
                String fileUrl = getFileUrl(uploadedFile.getId());

                return FileDTO.builder()
                        .fileName(uploadedFile.getName())
                        .fileUrl(fileUrl)
                        .contentType(uploadedFile.getMimeType())
                        .size(uploadedFile.getSize() != null ? uploadedFile.getSize() : file.getSize())
                        .bucketName(departmentName + "/" + meeting.getTitle())
                        .build();

            } finally {
                // Clean up temp file
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }

        } catch (Exception e) {
            log.error("Error uploading file to Google Drive: ", e);
            throw new BadRequestException("Could not upload file to storage: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(Long meetingId, String fileName) {
        try {
            // Get meeting information
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

            // Get department from meeting -> room -> department
            Room room = meeting.getRoom();
            if (room == null || room.getDepartment() == null) {
                throw new BadRequestException("Meeting does not have an associated department");
            }

            Department department = room.getDepartment();
            String departmentName = department.getDepartmentName() != null 
                    ? department.getDepartmentName() 
                    : department.getDepartmentCode();

            // Get folder structure
            String departmentFolderId = getOrCreateDepartmentFolder(departmentName);
            String meetingFolderId = getOrCreateMeetingFolder(departmentFolderId, meeting.getTitle());

            // Find file by name in the meeting folder
            String fileId = findFileId(meetingFolderId, fileName);

            if (fileId == null) {
                throw new BadRequestException("File not found: " + fileName);
            }

            // Download file content
            return googleDriveClient.files().get(fileId).executeMediaAsInputStream();

        } catch (Exception e) {
            log.error("Error downloading file from Google Drive: ", e);
            throw new BadRequestException("Could not download file from storage: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(Long meetingId, String fileName) {
        try {
            // Get meeting information
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

            // Get department from meeting -> room -> department
            Room room = meeting.getRoom();
            if (room == null || room.getDepartment() == null) {
                throw new BadRequestException("Meeting does not have an associated department");
            }

            Department department = room.getDepartment();
            String departmentName = department.getDepartmentName() != null 
                    ? department.getDepartmentName() 
                    : department.getDepartmentCode();

            // Get folder structure
            String departmentFolderId = getOrCreateDepartmentFolder(departmentName);
            String meetingFolderId = getOrCreateMeetingFolder(departmentFolderId, meeting.getTitle());

            // Find file by name in the meeting folder
            String fileId = findFileId(meetingFolderId, fileName);

            if (fileId == null) {
                throw new BadRequestException("File not found: " + fileName);
            }

            // Delete file
            googleDriveClient.files().delete(fileId).execute();
            log.info("File deleted successfully: {}", fileName);

        } catch (Exception e) {
            log.error("Error deleting file from Google Drive: ", e);
            throw new BadRequestException("Could not delete file from storage: " + e.getMessage());
        }
    }

    @Override
    public String getFileUrl(Long meetingId, String fileName) {
        try {
            // Get meeting information
            Meeting meeting = meetingRepository.findById(meetingId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

            // Get department from meeting -> room -> department
            Room room = meeting.getRoom();
            if (room == null || room.getDepartment() == null) {
                throw new BadRequestException("Meeting does not have an associated department");
            }

            Department department = room.getDepartment();
            String departmentName = department.getDepartmentName() != null 
                    ? department.getDepartmentName() 
                    : department.getDepartmentCode();

            // Get folder structure
            String departmentFolderId = getOrCreateDepartmentFolder(departmentName);
            String meetingFolderId = getOrCreateMeetingFolder(departmentFolderId, meeting.getTitle());

            // Find file by name in the meeting folder
            String fileId = findFileId(meetingFolderId, fileName);

            if (fileId == null) {
                throw new BadRequestException("File not found: " + fileName);
            }

            return getFileUrl(fileId);

        } catch (Exception e) {
            log.error("Error getting file URL from Google Drive: ", e);
            throw new BadRequestException("Could not get file URL: " + e.getMessage());
        }
    }

    @Override
    public String getOrCreateDepartmentFolder(String departmentName) {
        try {
            // Try to find existing department folder (root level)
            String folderId = findFolderByName(null, departmentName);
            
            if (folderId != null) {
                return folderId;
            }

            // Create new department folder at root level
            File folderMetadata = new File();
            folderMetadata.setName(departmentName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");

            File folder = googleDriveClient.files().create(folderMetadata)
                    .setFields("id")
                    .execute();

            log.info("Created department folder: {} with ID: {}", departmentName, folder.getId());
            return folder.getId();

        } catch (Exception e) {
            log.error("Error getting or creating department folder: {}", e.getMessage());
            throw new BadRequestException("Could not get or create department folder: " + e.getMessage());
        }
    }

    @Override
    public String getOrCreateMeetingFolder(String departmentFolderId, String meetingTitle) {
        try {
            // Try to find existing meeting folder under department folder
            String folderId = findFolderByName(departmentFolderId, meetingTitle);
            
            if (folderId != null) {
                return folderId;
            }

            // Create new meeting folder under department folder
            File folderMetadata = new File();
            folderMetadata.setName(meetingTitle);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(departmentFolderId));

            File folder = googleDriveClient.files().create(folderMetadata)
                    .setFields("id")
                    .execute();

            log.info("Created meeting folder: {} with ID: {} under department folder: {}", 
                    meetingTitle, folder.getId(), departmentFolderId);
            return folder.getId();

        } catch (Exception e) {
            log.error("Error getting or creating meeting folder: {}", e.getMessage());
            throw new BadRequestException("Could not get or create meeting folder: " + e.getMessage());
        }
    }

    /**
     * Find folder by name. If parentFolderId is null, searches at root level.
     * If parentFolderId is provided, searches within that folder.
     */
    private String findFolderByName(String parentFolderId, String folderName) {
        try {
            String query;
            if (parentFolderId != null) {
                query = "name='" + folderName.replace("'", "\\'") + "' " +
                       "and mimeType='application/vnd.google-apps.folder' " +
                       "and '" + parentFolderId + "' in parents " +
                       "and trashed=false";
            } else {
                query = "name='" + folderName.replace("'", "\\'") + "' " +
                       "and mimeType='application/vnd.google-apps.folder' " +
                       "and trashed=false";
            }
            
            FileList result = googleDriveClient.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0).getId();
            }
            return null;
        } catch (Exception e) {
            log.error("Error finding folder: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Find file by name within a folder
     * Supports exact match or pattern match (files with timestamp prefix)
     */
    private String findFileId(String folderId, String fileName) {
        try {
            // First try exact match
            String escapedFileName = fileName.replace("'", "\\'").replace("\\", "\\\\");
            String query = "name='" + escapedFileName + "' " +
                          "and '" + folderId + "' in parents " +
                          "and trashed=false";
            
            FileList result = googleDriveClient.files().list()
                    .setQ(query)
                    .setFields("files(id, name)")
                    .execute();

            List<File> files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                return files.get(0).getId();
            }
            
            // If exact match fails, try pattern match (for files with timestamp prefix)
            // Search for files that end with the fileName
            String patternQuery = "name contains '" + escapedFileName + "' " +
                                "and '" + folderId + "' in parents " +
                                "and trashed=false";
            
            result = googleDriveClient.files().list()
                    .setQ(patternQuery)
                    .setFields("files(id, name)")
                    .execute();

            files = result.getFiles();
            if (files != null && !files.isEmpty()) {
                // Find the file that ends with the requested fileName
                for (File file : files) {
                    if (file.getName().endsWith(fileName)) {
                        return file.getId();
                    }
                }
                // If no exact end match, return the first one found
                return files.get(0).getId();
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error finding file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get file URL from Google Drive file ID
     */
    private String getFileUrl(String fileId) {
        try {
            File file = googleDriveClient.files().get(fileId)
                    .setFields("webViewLink, webContentLink")
                    .execute();

            // Prefer webContentLink for direct download, fallback to webViewLink
            if (file.getWebContentLink() != null) {
                return file.getWebContentLink();
            }
            return file.getWebViewLink();
        } catch (Exception e) {
            log.error("Error getting file URL: {}", e.getMessage());
            // Return a basic URL format if API call fails
            return "https://drive.google.com/file/d/" + fileId + "/view";
        }
    }
}
