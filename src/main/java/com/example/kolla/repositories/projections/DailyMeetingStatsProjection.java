package com.example.kolla.repositories.projections;

import java.time.LocalDate;

public interface DailyMeetingStatsProjection {
    LocalDate getStatsDate();
    Long getMeetingCount();
    Long getParticipantCount();
}


