package com.example.kolla.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentEditLogCreateDTO {
    @NotNull(message = "Meeting id is required")
    private Long meetingId;

    @NotNull(message = "Editor user id is required")
    private Long editedById;

    @NotBlank(message = "Change summary is required")
    private String changeSummary;
}

