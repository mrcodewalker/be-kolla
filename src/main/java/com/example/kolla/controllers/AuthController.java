package com.example.kolla.controllers;

import java.util.Date;

import com.example.kolla.responses.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.example.kolla.dto.UserCreateDTO;
import com.example.kolla.dto.request.ChangePasswordRequest;
import com.example.kolla.dto.request.LoginRequest;
import com.example.kolla.responses.AuthResponse;
import com.example.kolla.models.User;
import com.example.kolla.services.impl.JwtService;
import com.example.kolla.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Operation(summary = "Login user", description = "Authenticate a user and return a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        User user = userService.login(request.getEmail(), request.getPassword(), httpServletRequest);
        String token = jwtService.generateToken(user);
        
        return ResponseEntity.ok(mapToAuthResponse(user, token));
    }

    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserCreateDTO userCreateDTO, HttpServletRequest request) {
        return ResponseEntity.ok(userService.createUser(userCreateDTO, request));
    }

    @Operation(summary = "Change password", description = "User changes their own password. Requires authentication.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password successfully changed"),
        @ApiResponse(responseCode = "400", description = "Invalid old password or validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - user not authenticated")
    })
    @PostMapping("/change-password")
    public ResponseEntity<com.example.kolla.responses.ApiResponse<UserResponse>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, 
            HttpServletRequest httpServletRequest) {
        UserResponse userResponse = userService.updateMyPassword(
            request.getOldPassword(), 
            request.getNewPassword(), 
            httpServletRequest
        );
        return ResponseEntity.ok(com.example.kolla.responses.ApiResponse.success("Password changed successfully", userResponse));
    }

    private AuthResponse mapToAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .response(UserResponse.mapToResponse(user))
                .build();
    }
}