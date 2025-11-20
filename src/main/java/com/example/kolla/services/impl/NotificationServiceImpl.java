package com.example.kolla.services.impl;

import com.example.kolla.dto.NotificationDTO;
import com.example.kolla.responses.NotificationResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.exceptions.ResourceNotFoundException;
import com.example.kolla.models.*;
import com.example.kolla.repositories.NotificationRepository;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.repositories.MeetingRepository;
import com.example.kolla.services.NotificationService;
import com.example.kolla.utils.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;

    @Override
    @Transactional
    public void createNotification(NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        notification.setTitle(notificationDTO.getTitle());
        notification.setContent(notificationDTO.getContent());
        notification.setType(notificationDTO.getNotificationType());
        notification.setSender(
                this.userRepository.findById(
                        notificationDTO.getSenderId()
                ).orElseThrow(() -> new ResourceNotFoundException("Can not find user with id: "+notificationDTO.getSenderId()))
        );
        if (notificationDTO.getReceiverId() != null) {
            notification.setReceiver(
                    this.userRepository.findById(notificationDTO.getReceiverId())
                            .orElseThrow(() -> new ResourceNotFoundException("Can not find receiver user with id: "+notificationDTO.getReceiverId()))
            );
        }
        if (notificationDTO.getMeetingId() != null) {
            notification.setMeeting(
                    this.meetingRepository.findById(notificationDTO.getMeetingId())
                            .orElse(null) // Optional: không throw exception nếu meeting không tồn tại
            );
        }
        notification.setIsRead(false); // Mặc định notification chưa được đọc
        notification.setCreatedAt(DateTimeUtils.now());
        notification.setUpdatedAt(DateTimeUtils.now());
        
        notificationRepository.save(notification);
    }

    @Override
    public PageResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageRequest);
        
        return createPageResponse(notificationPage);
    }

    @Override
    public PageResponse<NotificationResponse> getUnreadNotifications(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Notification> notificationPage = notificationRepository.findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageRequest);
        
        return createPageResponse(notificationPage);
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponse getNotificationById(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        // Check if user has access to this notification (user must be the receiver)
        if (notification.getReceiver() == null || !notification.getReceiver().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        
        // Tự động đánh dấu notification là đã đọc khi user xem chi tiết
        if (notification.getIsRead() == null || !notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setUpdatedAt(DateTimeUtils.now());
            notificationRepository.save(notification);
        }
        
        return NotificationResponse.mapToResponse(notification);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        // Check if user has access to this notification (user must be the receiver)
        if (notification.getReceiver() == null || !notification.getReceiver().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        
        notification.setIsRead(true);
        notification.setUpdatedAt(DateTimeUtils.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverIdAndIsReadFalse(userId);
        
        LocalDateTime now = DateTimeUtils.now();
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setUpdatedAt(now);
        });
        
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        
        // Check if user has access to this notification (user must be the receiver)
        if (notification.getReceiver() == null || !notification.getReceiver().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void deleteAllNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByReceiverId(userId);
        notificationRepository.deleteAll(notifications);
    }

    @Override
    @Transactional
    public void markMultipleAsRead(List<Long> notificationIds, Long userId) {
        List<Notification> notifications = notificationRepository.findByIdInAndReceiverId(notificationIds, userId);
        
        LocalDateTime now = DateTimeUtils.now();
        notifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setUpdatedAt(now);
        });
        
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional
    public void deleteMultipleNotifications(List<Long> notificationIds, Long userId) {
        List<Notification> notifications = notificationRepository.findByIdInAndReceiverId(notificationIds, userId);
        notificationRepository.deleteAll(notifications);
    }

    private PageResponse<NotificationResponse> createPageResponse(Page<Notification> notificationPage) {
        PageResponse<NotificationResponse> response = new PageResponse<>();
        response.setContent(notificationPage.getContent().stream()
            .map(NotificationResponse::mapToResponse)
            .collect(Collectors.toList()));
        response.setPageNumber(notificationPage.getNumber());
        response.setPageSize(notificationPage.getSize());
        response.setTotalElements(notificationPage.getTotalElements());
        response.setTotalPages(notificationPage.getTotalPages());
        response.setLast(notificationPage.isLast());
        
        return response;
    }
}