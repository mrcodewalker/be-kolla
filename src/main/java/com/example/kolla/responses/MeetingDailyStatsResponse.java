package com.example.kolla.responses;

import com.example.kolla.repositories.projections.DailyMeetingStatsProjection;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDailyStatsResponse {
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;
    private Long meetingCount;
    private Long participantCount;

    public static MeetingDailyStatsResponse fromProjection(DailyMeetingStatsProjection projection) {
        return MeetingDailyStatsResponse.builder()
                .date(projection.getStatsDate())
                .meetingCount(projection.getMeetingCount())
                .participantCount(projection.getParticipantCount())
                .build();
    }
}


