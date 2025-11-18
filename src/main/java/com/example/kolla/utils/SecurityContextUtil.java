package com.example.kolla.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.example.kolla.exceptions.UnauthorizedException;
import com.example.kolla.models.User;

/**
 * Utility class để lấy thông tin user từ SecurityContext
 * Cung cấp method để lấy User object hiện tại
 */
@Component
public class SecurityContextUtil {

    /**
     * Lấy User object hiện tại từ SecurityContext
     * @return User object của user hiện tại
     * @throws UnauthorizedException nếu không có authentication
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof UserDetails) {
            // Nếu principal là UserDetails, cần load User từ database
            // Có thể cần inject UserService để load User từ database
            throw new UnauthorizedException("Unable to get User object from UserDetails");
        }
        
        throw new UnauthorizedException("Invalid authentication principal");
    }
}
