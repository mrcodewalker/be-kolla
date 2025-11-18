package com.example.kolla.repositories;

import com.example.kolla.models.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long>, JpaSpecificationExecutor<AttendanceLog> {
    List<AttendanceLog> findByMeetingId(Long meetingId);
    void deleteByMeetingId(Long meetingId);
    List<AttendanceLog> findByUserId(Long userId);
}