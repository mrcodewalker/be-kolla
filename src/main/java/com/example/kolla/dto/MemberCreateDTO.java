package com.example.kolla.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCreateDTO {
    @NotNull(message = "Meeting ID is required")
    private Long meetingId;
}
