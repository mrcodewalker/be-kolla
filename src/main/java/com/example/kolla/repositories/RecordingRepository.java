package com.example.kolla.repositories;

import com.example.kolla.models.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecordingRepository extends JpaRepository<Recording, Long>, JpaSpecificationExecutor<Recording> {
    Page<Recording> findByMeetingId(Long meetingId, Pageable pageable);
    List<Recording> findByMeetingId(Long meetingId);
    void deleteByMeetingId(Long meetingId);
    List<Recording> findByCreatedById(Long userId);
}