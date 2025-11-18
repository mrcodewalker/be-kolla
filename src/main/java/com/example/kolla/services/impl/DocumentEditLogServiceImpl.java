package com.example.kolla.services.impl;

import com.example.kolla.dto.DocumentEditLogDTO;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.DocumentEditLog;
import com.example.kolla.repositories.DocumentEditLogRepository;
import com.example.kolla.services.DocumentEditLogService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentEditLogServiceImpl implements DocumentEditLogService {

    private final DocumentEditLogRepository documentEditLogRepository;
    private final ModelMapper modelMapper;

    @Override
    public DocumentEditLogDTO createLog(DocumentEditLogDTO logDTO) {
        DocumentEditLog log = modelMapper.map(logDTO, DocumentEditLog.class);
        log.setEditedAt(LocalDateTime.now());
        log = documentEditLogRepository.save(log);
        return modelMapper.map(log, DocumentEditLogDTO.class);
    }

    @Override
    public DocumentEditLogDTO getLogById(Long id) {
        DocumentEditLog log = documentEditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document edit log not found"));
        return modelMapper.map(log, DocumentEditLogDTO.class);
    }

    @Override
    public List<DocumentEditLogDTO> getLogsByDocumentId(Long documentId) {
        return documentEditLogRepository.findByDocumentId(documentId).stream()
                .map(log -> modelMapper.map(log, DocumentEditLogDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentEditLogDTO> getLogsByUserId(Long userId) {
        return documentEditLogRepository.findByEditedById(userId).stream()
                .map(log -> modelMapper.map(log, DocumentEditLogDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<DocumentEditLogDTO> getAllLogs() {
        return documentEditLogRepository.findAll().stream()
                .map(log -> modelMapper.map(log, DocumentEditLogDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLog(Long id) {
        if (!documentEditLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document edit log not found");
        }
        documentEditLogRepository.deleteById(id);
    }

    @Override
    public Page<DocumentEditLogDTO> getDocumentHistory(Long documentId, Pageable pageable) {
        return documentEditLogRepository.findByDocumentIdOrderByEditedAtDesc(documentId, pageable)
                .map(log -> modelMapper.map(log, DocumentEditLogDTO.class));
    }
}