package com.example.kolla.repositories;

import com.example.kolla.models.Meeting;
import com.example.kolla.repositories.projections.DailyMeetingStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("""
        SELECT function('DATE', mt.startTime) AS statsDate,
               COUNT(DISTINCT mt.id) AS meetingCount,
               COUNT(DISTINCT mem.id) AS participantCount
        FROM Meeting mt
        LEFT JOIN mt.members mem WITH mem.isActive = true
        WHERE (:startDate IS NULL OR mt.startTime >= :startDate)
          AND (:endDate IS NULL OR mt.startTime <= :endDate)
        GROUP BY function('DATE', mt.startTime)
        ORDER BY statsDate ASC
        """)
    List<DailyMeetingStatsProjection> aggregateDailyStats(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}