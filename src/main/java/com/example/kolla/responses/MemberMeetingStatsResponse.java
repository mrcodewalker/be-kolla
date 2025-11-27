package com.example.kolla.responses;

import com.example.kolla.repositories.projections.MemberMeetingCountProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberMeetingStatsResponse {
    private Long userId;
    private String name;
    private String email;
    private Long meetingCount;

    public static MemberMeetingStatsResponse fromProjection(MemberMeetingCountProjection projection) {
        String resolvedName = projection.getUserName();
        if (resolvedName == null || resolvedName.isBlank()) {
            resolvedName = projection.getUserEmail();
        }
        return MemberMeetingStatsResponse.builder()
                .userId(projection.getUserId())
                .name(resolvedName)
                .email(projection.getUserEmail())
                .meetingCount(projection.getMeetingCount())
                .build();
    }
}


