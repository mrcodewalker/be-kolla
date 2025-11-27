package com.example.kolla.repositories;

import com.example.kolla.models.Member;
import com.example.kolla.repositories.projections.MemberMeetingCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    
    // Tìm member theo user và meeting
    Optional<Member> findByUserIdAndMeetingId(Long userId, Long meetingId);
    boolean existsByUserIdAndMeetingId(Long userId, Long meetingId);
    
    // Kiểm tra user đã là member của meeting chưa
    boolean existsByUserIdAndMeetingIdAndIsActive(Long userId, Long meetingId, boolean isActive);
    void deleteByUserIdAndMeetingId(Long userId, Long meetingId);
    
    // Kiểm tra meeting có member active không
    boolean existsByMeetingIdAndIsActive(Long meetingId, boolean isActive);

    long countByMeetingIdAndIsActive(Long meetingId, boolean isActive);
    
    // Back-compat helpers by roomId via meeting.room.id
    Optional<Member> findByUserIdAndMeetingRoomId(Long userId, Long roomId);
    boolean existsByUserIdAndMeetingRoomIdAndIsActive(Long userId, Long roomId, boolean isActive);
    boolean existsByMeetingRoomIdAndIsActive(Long roomId, boolean isActive);
    long countByMeetingRoomIdAndIsActive(Long roomId, boolean isActive);
    void deleteByUserIdAndMeetingRoomId(Long userId, Long roomId);

    // Lấy danh sách Member với Meeting để có thể lấy isActive và meeting info cùng lúc
    @Query("SELECT m FROM Member m JOIN FETCH m.meeting WHERE m.user.id = :userId")
    List<Member> findMembersWithMeetingByUserId(@Param("userId") Long userId);
    
    // Đếm số lượng ADMIN members trong một meeting (active)
    @Query("SELECT COUNT(m) FROM Member m WHERE m.meeting.id = :meetingId AND m.isActive = true AND m.role.name = 'ADMIN'")
    long countAdminMembersByMeetingId(@Param("meetingId") Long meetingId);

    @Query("""
        SELECT m.user.id AS userId,
               m.user.name AS userName,
               m.user.email AS userEmail,
               COUNT(DISTINCT m.meeting.id) AS meetingCount
        FROM Member m
        WHERE m.isActive = true
          AND (:startDate IS NULL OR m.meeting.startTime >= :startDate)
          AND (:endDate IS NULL OR m.meeting.startTime <= :endDate)
        GROUP BY m.user.id, m.user.name, m.user.email
        ORDER BY meetingCount DESC
        """)
    List<MemberMeetingCountProjection> countMeetingsPerMember(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
