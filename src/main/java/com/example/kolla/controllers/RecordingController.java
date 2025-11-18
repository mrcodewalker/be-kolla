package com.example.kolla.controllers;

import com.example.kolla.dto.RecordingDTO;
import com.example.kolla.responses.RecordingResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.dto.search.SearchCriteria;
import com.example.kolla.services.RecordingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/recordings")
@RequiredArgsConstructor
@Tag(name = "Recording Management", description = "APIs for managing meeting recordings")
public class RecordingController {

    private final RecordingService recordingService;

    @Operation(summary = "Start recording for a meeting")
    @PostMapping("/meetings/{meetingId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<RecordingResponse>> startRecording(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Recording started successfully", recordingService.startRecording(meetingId, userId)));
    }

    @Operation(summary = "Stop recording")
    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<RecordingResponse>> stopRecording(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Recording stopped successfully", recordingService.stopRecording(id)));
    }

    @Operation(summary = "Get recording by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecordingResponse>> getRecordingById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(recordingService.getRecordingById(id)));
    }

    @Operation(summary = "Get recordings by meeting")
    @GetMapping("/meetings/{meetingId}")
    public ResponseEntity<ApiResponse<PageResponse<RecordingResponse>>> getRecordingsByMeeting(
            @PathVariable Long meetingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(recordingService.getRecordingsByMeeting(meetingId, page, size)));
    }

    @Operation(summary = "Search recordings")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<RecordingResponse>>> searchRecordings(
            @RequestBody SearchCriteria criteria) {
        return ResponseEntity.ok(ApiResponse.success(recordingService.searchRecordings(criteria)));
    }

    @Operation(summary = "Download recording")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadRecording(@PathVariable Long id) {
        RecordingResponse recording = recordingService.getRecordingById(id);
        byte[] content = recordingService.downloadRecording(id);
        
        ByteArrayResource resource = new ByteArrayResource(content);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recording.getFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "Upload recording")
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<RecordingResponse>> uploadRecording(
            @RequestParam("file") MultipartFile file,
            @RequestParam("meetingId") Long meetingId,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Recording uploaded successfully", recordingService.uploadRecording(meetingId, userId, file)));
    }

    @Operation(summary = "Delete recording")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRecording(@PathVariable Long id) {
        recordingService.deleteRecording(id);
        return ResponseEntity.ok(ApiResponse.success("Recording deleted successfully"));
    }
}