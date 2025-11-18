package com.example.kolla.repositories;

import com.example.kolla.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiverId(Long userId);
    
    Page<Notification> findByReceiverIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByReceiverIdAndIsReadFalse(Long userId);
    
    Page<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    long countByReceiverIdAndIsReadFalse(Long userId);
    
    List<Notification> findByIdInAndReceiverId(List<Long> ids, Long userId);
    
    // Keep old methods for backward compatibility if needed
    @Deprecated
    List<Notification> findBySenderId(Long userId);
    
    @Deprecated
    Page<Notification> findBySenderIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    @Deprecated
    List<Notification> findByIdInAndSenderId(List<Long> ids, Long userId);

    void deleteByMeetingId(Long meetingId);
}