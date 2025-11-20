package com.example.kolla.controllers;

import com.example.kolla.dto.DocumentEditLogCreateDTO;
import com.example.kolla.dto.DocumentEditLogDTO;
import com.example.kolla.dto.search.DocumentEditLogSearchDTO;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.services.DocumentEditLogService;
import com.example.kolla.utils.AuthorizationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/document-edit-logs")
@RequiredArgsConstructor
@Tag(name = "Document Edit Log Management", description = "APIs for managing meeting document edit logs")
public class DocumentEditLogController {

    private final DocumentEditLogService documentEditLogService;
    private final AuthorizationTokenService authorizationTokenService;

    @PostMapping
    @Operation(summary = "Create document edit log")
    public ResponseEntity<ApiResponse<DocumentEditLogDTO>> createDocumentEditLog(
            @Valid @RequestBody DocumentEditLogCreateDTO createDTO,
            HttpServletRequest request) {
        Long currentUserId = authorizationTokenService.extractUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        createDTO.setEditedById(currentUserId);
        DocumentEditLogDTO createdLog = documentEditLogService.createLog(createDTO);
        return ResponseEntity.ok(ApiResponse.success("Document edit log created successfully", createdLog));
    }

    @GetMapping("/search")
    @Operation(summary = "Search document edit logs with criteria")
    public ResponseEntity<ApiResponse<PageResponse<DocumentEditLogDTO>>> searchDocumentEditLogs(
            @Parameter(description = "Search keyword (change summary, meeting title, editor name)")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Meeting ID")
            @RequestParam(required = false) Long meetingId,
            @Parameter(description = "Editor user ID")
            @RequestParam(required = false) Long editedById,
            @Parameter(description = "Start date (filter by editedAt >=)")
            @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date (filter by editedAt <=)")
            @RequestParam(required = false) LocalDateTime endDate,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Sort by field")
            @RequestParam(defaultValue = "editedAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        DocumentEditLogSearchDTO searchDTO = new DocumentEditLogSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setMeetingId(meetingId);
        searchDTO.setEditedById(editedById);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        PageResponse<DocumentEditLogDTO> logs = documentEditLogService.searchLogs(searchDTO);
        return ResponseEntity.ok(ApiResponse.success("Document edit logs retrieved successfully", logs));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete document edit log")
    public ResponseEntity<ApiResponse<Void>> deleteDocumentEditLog(@PathVariable Long id) {
        documentEditLogService.deleteLog(id);
        return ResponseEntity.ok(ApiResponse.success("Document edit log deleted successfully"));
    }
}

