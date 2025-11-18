package com.example.kolla.dto.request;

import com.example.kolla.enums.Role;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private Role role = Role.USER; // Default role
}