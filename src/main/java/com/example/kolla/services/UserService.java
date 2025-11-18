package com.example.kolla.services;

import com.example.kolla.responses.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.kolla.dto.UserDTO;
import com.example.kolla.models.User;
import jakarta.servlet.http.HttpServletRequest;
import com.example.kolla.dto.UserCreateDTO;
import com.example.kolla.dto.search.UserSearchDTO;
import com.example.kolla.responses.PageResponse;

public interface UserService extends UserDetailsService {
    User login(String email, String password, HttpServletRequest request);
    UserResponse createUser(UserCreateDTO createDTO, HttpServletRequest request);
    UserResponse updateUser(Long id, UserDTO updateDTO, HttpServletRequest request);
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
    PageResponse<UserResponse> searchUsers(UserSearchDTO searchDTO);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
    UserResponse updateAvatar(Long id, String avatarUrl, HttpServletRequest request);
    UserResponse updatePassword(Long id, String oldPassword, String newPassword, HttpServletRequest request);
    UserResponse updateMyProfile(UserDTO updateDTO, HttpServletRequest request);
    UserResponse updateMyPassword(String oldPassword, String newPassword, HttpServletRequest request);
    java.util.List<UserResponse> searchByNameOrEmail(String keyword);
}