package com.example.kolla.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberApprovalDTO {
    @NotNull(message = "Meeting ID is required")
    private Long meetingId;

    @NotEmpty(message = "Approvals are required")
    private List<ApprovalItem> approvals;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalItem {
        @NotNull(message = "Member ID is required")
        private Long memberId;

        @NotNull(message = "Role ID is required")
        private Long roleId;
    }
}
