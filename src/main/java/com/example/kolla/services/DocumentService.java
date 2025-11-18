package com.example.kolla.services;

import com.example.kolla.dto.search.DocumentSearchDTO;
import com.example.kolla.responses.DocumentResponse;
import com.example.kolla.responses.PageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    DocumentResponse uploadDocument(Long meetingId, Long userId, MultipartFile file, String description);
    DocumentResponse getDocumentById(Long id);
    DocumentResponse updateDocument(Long id, String description);
    void deleteDocument(Long id);
    byte[] downloadDocument(Long id);
    PageResponse<DocumentResponse> searchDocuments(DocumentSearchDTO searchDTO);
}