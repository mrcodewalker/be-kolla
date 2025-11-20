package com.example.kolla.services;

import com.example.kolla.dto.DocumentEditLogCreateDTO;
import com.example.kolla.dto.DocumentEditLogDTO;
import com.example.kolla.dto.search.DocumentEditLogSearchDTO;
import com.example.kolla.responses.PageResponse;

public interface DocumentEditLogService {
    DocumentEditLogDTO createLog(DocumentEditLogCreateDTO createDTO);
    DocumentEditLogDTO getLogById(Long id);
    PageResponse<DocumentEditLogDTO> searchLogs(DocumentEditLogSearchDTO searchDTO);
    void deleteLog(Long id);
}
