package com.example.kolla.services;

import com.example.kolla.dto.NotificationDTO;
import com.example.kolla.responses.NotificationResponse;
import com.example.kolla.responses.PageResponse;

import java.util.List;

public interface NotificationService {
    // Create notifications
    void createNotification(NotificationDTO notificationDTO);

    // Read notifications
    PageResponse<NotificationResponse> getUserNotifications(Long userId, int page, int size);
    
    PageResponse<NotificationResponse> getUnreadNotifications(Long userId, int page, int size);
    
    long countUnreadNotifications(Long userId);
    
    NotificationResponse getNotificationById(Long notificationId, Long userId);
    
    // Update notifications
    void markAsRead(Long notificationId, Long userId);
    
    void markAllAsRead(Long userId);
    
    // Delete notifications
    void deleteNotification(Long notificationId, Long userId);
    
    void deleteAllNotifications(Long userId);

    void markMultipleAsRead(List<Long> notificationIds, Long userId);
    
    void deleteMultipleNotifications(List<Long> notificationIds, Long userId);
}