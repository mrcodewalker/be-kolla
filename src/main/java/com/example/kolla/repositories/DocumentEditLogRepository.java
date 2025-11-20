package com.example.kolla.repositories;

import com.example.kolla.models.DocumentEditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentEditLogRepository extends JpaRepository<DocumentEditLog, Long>, JpaSpecificationExecutor<DocumentEditLog> {
    List<DocumentEditLog> findByMeetingId(Long meetingId);
    List<DocumentEditLog> findByEditedById(Long userId);
    Page<DocumentEditLog> findByMeetingIdOrderByEditedAtDesc(Long meetingId, Pageable pageable);
    void deleteByMeetingId(Long meetingId);
}