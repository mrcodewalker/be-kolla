package com.example.kolla.services;

import com.example.kolla.dto.DocumentEditLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DocumentEditLogService {
    DocumentEditLogDTO createLog(DocumentEditLogDTO logDTO);
    DocumentEditLogDTO getLogById(Long id);
    List<DocumentEditLogDTO> getLogsByDocumentId(Long documentId);
    List<DocumentEditLogDTO> getLogsByUserId(Long userId);
    List<DocumentEditLogDTO> getAllLogs();
    void deleteLog(Long id);
    Page<DocumentEditLogDTO> getDocumentHistory(Long documentId, Pageable pageable);
}