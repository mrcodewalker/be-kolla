package com.example.kolla.services.impl;

import com.example.kolla.dto.DocumentEditLogCreateDTO;
import com.example.kolla.dto.DocumentEditLogDTO;
import com.example.kolla.dto.search.DocumentEditLogSearchDTO;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.DocumentEditLog;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.User;
import com.example.kolla.repositories.DocumentEditLogRepository;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.services.DocumentEditLogService;
import com.example.kolla.specifications.DocumentEditLogSpecifications;
import com.example.kolla.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentEditLogServiceImpl implements DocumentEditLogService {

    private final DocumentEditLogRepository documentEditLogRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Override
    public DocumentEditLogDTO createLog(DocumentEditLogCreateDTO createDTO) {
        Meeting meeting = meetingRepository.findById(createDTO.getMeetingId())
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + createDTO.getMeetingId()));

        User editor = userRepository.findById(createDTO.getEditedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + createDTO.getEditedById()));

        DocumentEditLog log = new DocumentEditLog();
        log.setMeeting(meeting);
        log.setEditedBy(editor);
        log.setChangeSummary(createDTO.getChangeSummary());
        log.setEditedAt(DateTimeUtils.now());

        DocumentEditLog savedLog = documentEditLogRepository.save(log);
        return DocumentEditLogDTO.fromEntity(savedLog);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentEditLogDTO getLogById(Long id) {
        DocumentEditLog log = documentEditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document edit log not found with id: " + id));
        return DocumentEditLogDTO.fromEntity(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentEditLogDTO> searchLogs(DocumentEditLogSearchDTO searchDTO) {
        Specification<DocumentEditLog> specification = DocumentEditLogSpecifications.withSearchCriteria(searchDTO);

        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "editedAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection != null && sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<DocumentEditLog> logPage = documentEditLogRepository.findAll(specification, pageable);
        List<DocumentEditLogDTO> content = logPage.getContent().stream()
                .map(DocumentEditLogDTO::fromEntity)
                .collect(Collectors.toList());

        PageResponse<DocumentEditLogDTO> response = new PageResponse<>();
        response.setContent(content);
        response.setPageNumber(logPage.getNumber());
        response.setPageSize(logPage.getSize());
        response.setTotalElements(logPage.getTotalElements());
        response.setTotalPages(logPage.getTotalPages());
        response.setLast(logPage.isLast());
        return response;
    }

    @Override
    public void deleteLog(Long id) {
        if (!documentEditLogRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document edit log not found with id: " + id);
        }
        documentEditLogRepository.deleteById(id);
    }
}
