package com.example.kolla.controllers;

import com.example.kolla.dto.search.DocumentSearchDTO;
import com.example.kolla.responses.DocumentResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.services.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for managing documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @Operation(summary = "Create document", description = "Upload a new document")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("meetingId") Long meetingId,
            @RequestParam(value = "description", required = false) String description) {
        Long userId = Long.parseLong(userDetails.getUsername());
        DocumentResponse document = documentService.uploadDocument(meetingId, userId, file, description);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", document));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Get document information by ID")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update document", description = "Update document description")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECRETARY')")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @Parameter(description = "Document ID") @PathVariable Long id,
            @RequestParam String description) {
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", 
            documentService.updateDocument(id, description)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document", description = "Delete document by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents with criteria", description = "Search documents with flexible criteria and pagination")
    public ResponseEntity<ApiResponse<PageResponse<DocumentResponse>>> searchDocuments(
            @Parameter(description = "Search keyword (fileName, userName, meetingTitle)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Meeting ID") @RequestParam(required = false) Long meetingId,
            @Parameter(description = "User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "File type") @RequestParam(required = false) String fileType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        DocumentSearchDTO searchDTO = new DocumentSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setMeetingId(meetingId);
        searchDTO.setUserId(userId);
        searchDTO.setFileType(fileType);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);
        
        PageResponse<DocumentResponse> response = documentService.searchDocuments(searchDTO);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document", description = "Download document file")
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "Document ID") @PathVariable Long id) {
        DocumentResponse document = documentService.getDocumentById(id);
        byte[] content = documentService.downloadDocument(id);
        
        ByteArrayResource resource = new ByteArrayResource(content);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"")
                .body(resource);
    }
}