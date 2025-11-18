package com.example.kolla.services;

import com.example.kolla.dto.search.UserSessionSearchDTO;
import com.example.kolla.models.UserSession;
import com.example.kolla.responses.PageResponse;
import com.example.kolla.responses.UserSessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface UserSessionService {
    UserSession saveUserSession(UserSession userSession, HttpServletRequest request);
    String getClientIp(HttpServletRequest request);
    String getLocationFromIp(String ip);
    UserSessionResponse getUserSessionById(Long id);
    List<UserSessionResponse> getUserSessionsByUserId(Long userId);
    PageResponse<UserSessionResponse> searchUserSessions(UserSessionSearchDTO searchDTO);
    void deleteUserSession(Long id);
}