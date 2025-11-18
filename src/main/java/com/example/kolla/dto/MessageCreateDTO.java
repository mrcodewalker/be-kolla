package com.example.kolla.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageCreateDTO {
    @NotBlank(message = "Message không được để trống")
    private String message;

    @NotBlank(message = "Meeting Link không được để trống")
    private String meetLink;

    @NotBlank(message = "Token không được để trống")
    private String token;
}
