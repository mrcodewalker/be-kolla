package com.example.kolla.services.impl;

import com.example.kolla.dto.FileDTO;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.services.FileService;
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
public class FileServiceImpl implements FileService {

    private final Drive googleDriveClient;

    @Override
    public FileDTO uploadFile(String bucketName, MultipartFile file) {
        try {
            // Get folder ID - use bucketName as folder name or use default folder
            String folderId = getOrCreateFolder(bucketName);

            // Create file metadata
            File fileMetadata = new File();
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));

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
                        .bucketName(bucketName)
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
    public InputStream downloadFile(String bucketName, String fileName) {
        try {
            // Find file by name in the folder
            String folderId = getOrCreateFolder(bucketName);
            String fileId = findFileId(folderId, fileName);

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
    public void deleteFile(String bucketName, String fileName) {
        try {
            // Find file by name in the folder
            String folderId = getOrCreateFolder(bucketName);
            String fileId = findFileId(folderId, fileName);

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
    public String getFileUrl(String bucketName, String fileName) {
        try {
            // Find file by name in the folder
            String folderId = getOrCreateFolder(bucketName);
            String fileId = findFileId(folderId, fileName);

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
    public boolean createBucket(String bucketName) {
        try {
            // In Google Drive, buckets are folders
            String folderId = getOrCreateFolder(bucketName);
            return folderId != null;
        } catch (Exception e) {
            log.error("Error creating bucket (folder) in Google Drive: ", e);
            throw new BadRequestException("Could not create bucket: " + e.getMessage());
        }
    }

    private String getOrCreateFolder(String folderName) {
        try {
            // Try to find existing folder
            String folderId = findFolderByName(folderName);
            
            if (folderId != null) {
                return folderId;
            }

            // Create new folder
            File folderMetadata = new File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");

            File folder = googleDriveClient.files().create(folderMetadata)
                    .setFields("id")
                    .execute();

            log.info("Created folder: {} with ID: {}", folderName, folder.getId());
            return folder.getId();

        } catch (Exception e) {
            log.error("Error getting or creating folder: {}", e.getMessage());
            throw new BadRequestException("Could not get or create folder: " + e.getMessage());
        }
    }

    private String findFolderByName(String folderName) {
        try {
            String query = "name='" + folderName + "' and mimeType='application/vnd.google-apps.folder' and trashed=false";
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

    private String findFileId(String folderId, String fileName) {
        try {
            String query = "name='" + fileName + "' and '" + folderId + "' in parents and trashed=false";
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
            log.error("Error finding file: {}", e.getMessage());
            return null;
        }
    }

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
