package com.example.kolla.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Login request payload")
public class LoginRequest {
    @Schema(description = "User email", example = "user@example.com")
    private String email;

    @Schema(description = "User password", example = "password123")
    private String password;
}