package com.example.kolla.repositories;

import com.example.kolla.models.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long>, JpaSpecificationExecutor<Meeting> {
    List<Meeting> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Meeting> findByRoomId(Long roomId);
    boolean existsByMeetingCode(String meetingCode);
    List<Meeting> findByRoomIdOrderByStartTimeDesc(Long roomId);
    List<Meeting> findByRoomIdInOrderByStartTimeAsc(List<Long> roomIds);
    Optional<Meeting> findByMeetingLink(String meetingLink);

    Page<Meeting> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);
}