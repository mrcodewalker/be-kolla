package com.example.kolla.repositories;

import com.example.kolla.models.Document;
import com.example.kolla.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findByMeetingId(Long meetingId);
    void deleteByMeetingId(Long meetingId);
    List<Document> findByFileType(FileType fileType);
    List<Document> findByUserId(Long userId);
}