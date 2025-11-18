package com.example.kolla.responses;

import com.example.kolla.models.UserSession;
import com.example.kolla.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserSessionResponse {
    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private String userAgent;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private String action;
    private Long userId;
    private String userName;
    private String userEmail;
    
    public static UserSessionResponse mapToResponse(UserSession userSession) {
        User user = userSession.getUser();
        
        return UserSessionResponse.builder()
                .id(userSession.getId())
                .deviceInfo(userSession.getDeviceInfo())
                .ipAddress(userSession.getIpAddress())
                .userAgent(userSession.getUserAgent())
                .location(userSession.getLocation())
                .createdAt(userSession.getCreatedAt())
                .updatedAt(userSession.getUpdatedAt())
                .isActive(userSession.isActive())
                .action(userSession.getAction())
                .userId(user != null ? user.getId() : null)
                .userName(user != null ? user.getName() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .build();
    }
}


