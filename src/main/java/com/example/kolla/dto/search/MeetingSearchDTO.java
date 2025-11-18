package com.example.kolla.dto.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MeetingSearchDTO extends SearchCriteria {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long createdBy;
    private Long roomId;
    private List<String> tags;
    private Boolean isRecording;
    private Long participantId;
}