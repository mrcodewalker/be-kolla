package com.example.kolla.controllers;

import com.example.kolla.dto.FileDTO;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.services.GoogleDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "APIs for managing files on Google Drive with hierarchical folder structure")
public class FileController {

    private final GoogleDriveService googleDriveService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file", description = "Upload file to Google Drive. Folder structure: Department (Level 1) -> Meeting Title (Level 2) -> Files (Level 3)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<FileDTO>> uploadFile(
            @Parameter(description = "Meeting ID", required = true) @RequestParam("meetingId") Long meetingId,
            @Parameter(description = "File to upload", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")))
            @RequestParam("file") MultipartFile file) {
        FileDTO fileDTO = googleDriveService.uploadFile(meetingId, file);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", fileDTO));
    }

    @GetMapping("/download/{meetingId}/{fileName}")
    @Operation(summary = "Download a file", description = "Download file from Google Drive by meeting ID and file name")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InputStreamResource> downloadFile(
            @Parameter(description = "Meeting ID") @PathVariable("meetingId") Long meetingId,
            @Parameter(description = "File name") @PathVariable("fileName") String fileName) {
        InputStream inputStream = googleDriveService.downloadFile(meetingId, fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/{meetingId}/{fileName}")
    @Operation(summary = "Delete a file", description = "Delete file from Google Drive by meeting ID and file name")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "Meeting ID") @PathVariable("meetingId") Long meetingId,
            @Parameter(description = "File name") @PathVariable("fileName") String fileName) {
        googleDriveService.deleteFile(meetingId, fileName);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }

    @GetMapping("/url/{meetingId}/{fileName}")
    @Operation(summary = "Get file URL", description = "Get Google Drive file URL by meeting ID and file name")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> getFileUrl(
            @Parameter(description = "Meeting ID") @PathVariable("meetingId") Long meetingId,
            @Parameter(description = "File name") @PathVariable("fileName") String fileName) {
        String url = googleDriveService.getFileUrl(meetingId, fileName);
        return ResponseEntity.ok(ApiResponse.success("File URL retrieved successfully", url));
    }
}