package com.example.kolla.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Change password request payload")
public class ChangePasswordRequest {
    @NotBlank(message = "Old password is required")
    @Schema(description = "Current password", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters")
    @Schema(description = "New password (minimum 6 characters)", example = "newPassword123")
    private String newPassword;
}


