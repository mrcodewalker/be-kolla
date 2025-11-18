package com.example.kolla.repositories;

import com.example.kolla.models.MeetingMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingMessageRepository extends JpaRepository<MeetingMessage, Long>, 
                                                JpaSpecificationExecutor<MeetingMessage> {
    Page<MeetingMessage> findByMeetingId(Long meetingId, Pageable pageable);
    List<MeetingMessage> findByMeetingIdAndSentAtBetween(Long meetingId, LocalDateTime start, LocalDateTime end);
    List<MeetingMessage> findBySenderId(Long userId);
    
    @Modifying
    @Query("DELETE FROM MeetingMessage m WHERE m.meeting.id = :meetingId")
    void deleteByMeetingId(Long meetingId);
}