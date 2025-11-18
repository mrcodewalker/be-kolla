package com.example.kolla.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AttendanceLogCreateDTO {
    @NotBlank(message = "Token không được để trống")
    private String token;

    @NotBlank(message = "Meeting link không được để trống")
    private String meetLink;
    @NotBlank(message = "Type of action")
    private String action;
}



