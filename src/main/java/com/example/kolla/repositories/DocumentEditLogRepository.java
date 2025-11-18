package com.example.kolla.repositories;

import com.example.kolla.models.DocumentEditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentEditLogRepository extends JpaRepository<DocumentEditLog, Long> {
    List<DocumentEditLog> findByDocumentId(Long documentId);
    List<DocumentEditLog> findByEditedById(Long userId);
    Page<DocumentEditLog> findByDocumentIdOrderByEditedAtDesc(Long documentId, Pageable pageable);
    void deleteByDocumentId(Long documentId);
}