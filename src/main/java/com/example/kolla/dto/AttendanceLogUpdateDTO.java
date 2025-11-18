package com.example.kolla.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AttendanceLogUpdateDTO {
    @NotBlank(message = "Token không được để trống")
    private String token;
}
