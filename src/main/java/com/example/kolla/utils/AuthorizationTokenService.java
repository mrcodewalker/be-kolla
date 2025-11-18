package com.example.kolla.utils;

import com.example.kolla.models.User;
import com.example.kolla.repositories.UserRepository;
import com.example.kolla.services.impl.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthorizationTokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public String extractBearerToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    public Long extractUserId(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token == null) {
            return null;
        }
        Long userId = jwtService.extractUserId(token);
        if (userId != null) {
            return userId;
        }
        String email = jwtService.extractUsername(token);
        if (email == null) {
            return null;
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.map(User::getId).orElse(null);
    }

    public User extractUser(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (token == null) {
            return null;
        }
        Long userId = jwtService.extractUserId(token);
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }
        String email = jwtService.extractUsername(token);
        if (email == null) {
            return null;
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    // Role checks are enforced within MemberService to reduce coupling.
}


