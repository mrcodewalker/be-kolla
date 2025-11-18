package com.example.kolla.responses;

import java.util.Date;

import com.example.kolla.enums.Degree;
import com.example.kolla.models.Department;
import com.example.kolla.models.Role;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Date tokenExpiration;
    @JsonProperty("user")
    private UserResponse response;
}