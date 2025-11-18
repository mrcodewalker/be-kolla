package com.example.kolla.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberInviteDTO {
    @NotNull
    private Long meetingId;
    @Size(min = 1, message = "At least one member is required")
    private List<InviteItem> members;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InviteItem {
        @NotNull
        private Long userId;
        @NotNull
        private Long roleId;
    }
}



