package com.example.kolla.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRejectDTO {
    @NotNull(message = "Meeting ID is required")
    private Long meetingId;

    @NotEmpty(message = "Rejects list cannot be empty")
    private List<RejectInfo> rejects;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectInfo {
        @NotNull(message = "Member ID is required")
        private Long memberId;
    }
}

