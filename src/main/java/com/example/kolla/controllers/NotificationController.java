package com.example.kolla.controllers;

import com.example.kolla.responses.NotificationResponse;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.ApiResponse;
import com.example.kolla.services.NotificationService;
import com.example.kolla.utils.AuthorizationTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthorizationTokenService authorizationTokenService;

    @Operation(summary = "Get current user's notifications by token", description = "Get current user's notifications sorted by created_at descending (most recent first)")
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUserNotificationsByToken(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId, page, size)));
    }

    @Operation(summary = "Get user notifications")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUserNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId, page, size)));
    }

    @Operation(summary = "Get unread notifications")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getUnreadNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadNotifications(userId, page, size)));
    }

    @Operation(summary = "Get unread notification count")
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(HttpServletRequest request) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "Get notification by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
            HttpServletRequest request,
            @PathVariable("id") Long notificationId) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success(notificationService.getNotificationById(notificationId, userId)));
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            HttpServletRequest request,
            @PathVariable("id") Long notificationId) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read successfully"));
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            HttpServletRequest request) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read successfully"));
    }

    @Operation(summary = "Delete notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            HttpServletRequest request,
            @PathVariable("id") Long notificationId) {
        Long userId = authorizationTokenService.extractUserId(request);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not authenticated"));
        }
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully"));
    }
}