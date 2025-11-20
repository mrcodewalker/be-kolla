package com.example.kolla.services.impl;

import com.example.kolla.dto.search.DocumentSearchDTO;
import com.example.kolla.responses.DocumentResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.enums.FileType;
import com.example.kolla.exceptions.BadRequestException;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.Document;
import com.example.kolla.models.Meeting;
import com.example.kolla.models.User;
import com.example.kolla.repositories.DocumentRepository;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.services.DocumentService;
import com.example.kolla.specifications.DocumentSpecifications;
import com.example.kolla.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DocumentResponse uploadDocument(Long meetingId, Long userId, MultipartFile file, String description) {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        Meeting meeting = meetingRepository.findById(meetingId)
            .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + meetingId));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));


        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FileType fileType = determineFileType(fileName);

        try {
            Document document = new Document();
            document.setFileName(fileName);
            document.setFileType(fileType.toString());
            document.setFileSize(file.getSize());
            document.setUser(user);
            document.setMeeting(meeting);
            document.setCreatedAt(DateTimeUtils.now());
            document.setFileContent(file.getBytes());

            Document savedDocument = documentRepository.save(document);
            return DocumentResponse.mapToResponse(savedDocument);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return DocumentResponse.mapToResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DocumentResponse> searchDocuments(DocumentSearchDTO searchDTO) {
        // Tạo specification từ search criteria
        Specification<Document> spec = DocumentSpecifications.withSearchCriteria(searchDTO);
        
        // Tạo sort
        String sortBy = searchDTO.getSortBy() != null ? searchDTO.getSortBy() : "createdAt";
        String sortDirection = searchDTO.getSortDirection() != null ? searchDTO.getSortDirection() : "desc";
        Sort sort = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        // Tạo pageable
        int page = searchDTO.getPage() != null ? searchDTO.getPage() : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize() : 10;
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Thực hiện query
        Page<Document> documentPage = documentRepository.findAll(spec, pageable);
        
        // Chuyển đổi sang response
        List<DocumentResponse> documentResponses = documentPage.getContent().stream()
                .map(DocumentResponse::mapToResponse)
                .collect(Collectors.toList());
        
        // Tạo PageResponse
        PageResponse<DocumentResponse> response = new PageResponse<>();
        response.setContent(documentResponses);
        response.setPageNumber(documentPage.getNumber());
        response.setPageSize(documentPage.getSize());
        response.setTotalElements(documentPage.getTotalElements());
        response.setTotalPages(documentPage.getTotalPages());
        response.setLast(documentPage.isLast());
        return response;
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Document not found with id: " + id);
        }
        documentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(Long id, String description) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        
        document.setUpdatedAt(DateTimeUtils.now());
        Document updatedDocument = documentRepository.save(document);
        return DocumentResponse.mapToResponse(updatedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDocument(Long id) {
        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + id));
        return document.getFileContent();
    }

    private FileType determineFileType(String fileName) {
        String extension = StringUtils.getFilenameExtension(fileName);
        if (extension == null) {
            return FileType.OTHER;
        }
        
        switch (extension.toLowerCase()) {
            case "pdf":
                return FileType.PDF;
            case "doc":
            case "docx":
                return FileType.WORD;
            case "xls":
            case "xlsx":
                return FileType.EXCEL;
            case "ppt":
            case "pptx":
                return FileType.POWERPOINT;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return FileType.IMAGE;
            case "txt":
                return FileType.TEXT;
            default:
                return FileType.OTHER;
        }
    }
}